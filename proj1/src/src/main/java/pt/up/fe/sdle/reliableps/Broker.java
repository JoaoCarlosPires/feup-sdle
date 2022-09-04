package pt.up.fe.sdle.reliableps;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Poller;
import pt.up.fe.sdle.reliableps.constants.Messages;
import pt.up.fe.sdle.reliableps.constants.ReplyMessages;
import pt.up.fe.sdle.reliableps.loggers.BrokerLogger;
import pt.up.fe.sdle.reliableps.messages.RouterMessage;
import pt.up.fe.sdle.reliableps.messages.actions.*;
import pt.up.fe.sdle.reliableps.sockets.RouterSocket;
import pt.up.fe.sdle.reliableps.sockets.ZSocket;
import pt.up.fe.sdle.reliableps.testapp.BrokerInterface;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.*;

public class Broker implements BrokerInterface {
    private final RouterSocket frontend;
    private final RouterSocket backend;
    private final Poller items;

    /*
    {
        topic: {
            subscriberId: subscriberPosition
        }
    }
     */
    private HashMap<String, HashMap<String, Integer>> topicSubscribers = new HashMap<>();
    private final String topicSubscribersFileName = "topicSubscribers";

    /*
    {
        topic: [ message1, message2, (...) ]
    }
     */
    private HashMap<String, List<String>> topicContents = new HashMap<>();
    private final String topicContentsFileName = "topicContents";
    private ScheduledExecutorService executor = null;

    public Broker(ZContext context, String frontport, String backport, boolean restoreState) throws Exception {
        frontend = new RouterSocket(context, frontport);
        backend = new RouterSocket(context, backport);

        items = this.createPoller(context, new RouterSocket[]{ frontend, backend });
        if (restoreState) restoreState();
    }

    public Broker(ZContext context, String frontport, String backport, int savingPeriod) throws Exception {
        this(context, frontport, backport, true);
        if (savingPeriod == 0) return;

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::saveState, 1, savingPeriod, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        //  Prepare our context and sockets
        try {
            ZContext context = new ZContext();

            int savingPeriod = 0;
            if (args.length == 1) savingPeriod = Integer.parseInt(args[0]);

            var broker = new Broker(context, "tcp://*:5001", "tcp://*:5002", savingPeriod);
            broker.connectToRmi();

            //  Switch messages between sockets
            while (!Thread.currentThread().isInterrupted()) {
                //  poll and memorize multipart detection
                broker.poll();
            }
        } catch(Exception e) {
            BrokerLogger.errorStarting(e.getMessage());
        }
    }

    public void poll() {
        items.poll();

        if (items.pollin(0)) {
            handleSubscriberMessage(frontend);
        }

        if (items.pollin(1)) {
            handlePublisherMessage(backend);
        }
    }

    public HashMap<String, HashMap<String, Integer>> getTopicSubscribers() {
        return topicSubscribers;
    }

    public void setTopicSubscribers(HashMap<String, HashMap<String, Integer>> topicSubscribers) {
        this.topicSubscribers = topicSubscribers;
    }

    public HashMap<String, List<String>> getTopicContents() {
        return topicContents;
    }

    public void setTopicContents(HashMap<String, List<String>> topicContents) {
        this.topicContents = topicContents;
    }

    public void kill() {
        frontend.close();
        backend.close();
        if (executor != null) executor.shutdown();
    }

    private Poller createPoller(ZContext context, ZSocket[] sockets) {
        Poller items = context.createPoller(sockets.length);
        for (var socket : sockets) {
            items.register(socket.getSocket(), Poller.POLLIN);
        }
        return items;
    }

    private void handleSubscriberMessage(RouterSocket socket) {
        RouterMessage request = socket.receiveMessage();
        String content = request.getContent();

        Action action = new Action(content);
        if (Objects.equals(action.getAction(), Messages.get))
            handleGetMessage(request, socket);
        else if (Objects.equals(action.getAction(), Messages.sub))
            handleSubMessage(request, socket);
        else if (Objects.equals(action.getAction(), Messages.unsub))
            handleUnsubMessage(request, socket);
        else if (Objects.equals(action.getAction(), Messages.received))
            handleRecMessage(request, socket);
    }

    private void handleGetMessage(RouterMessage getMessage, RouterSocket socket) {
        Get get;
        try {
            get = new Get(getMessage.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String topic = get.getTopic();
        String subscriberId = get.getSubscriberId();
        HashMap<String, Integer> subscribers = topicSubscribers.get(topic);
        if (subscribers == null) {
            sendNackMessage(getMessage.getTarget(), socket, ReplyMessages.topicDoesNotExist(topic));
            return;
        }

        Integer pos = subscribers.get(subscriberId);
        if (pos == null) {
            sendNackMessage(getMessage.getTarget(), socket, ReplyMessages.notSubscribed(topic));
            return;
        }

        List<String> contentEntry = topicContents.getOrDefault(topic, new ArrayList<>());
        if (contentEntry.size() > pos) {
            sendContentMessage(getMessage.getTarget(), socket, topic, contentEntry.get(pos));
            return;
        }
        sendNackMessage(getMessage.getTarget(), socket, ReplyMessages.couldNotGetMessage(topic));
    }

    private void handleRecMessage(RouterMessage recMessage, RouterSocket socket) {
        Received received;
        try {
            received = new Received(recMessage.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String topic = received.getTopic();
        String subscriberId = received.getSubscriberId();
        HashMap<String, Integer> subscribers = topicSubscribers.get(topic);
        Integer pos = subscribers.get(subscriberId);

        if (pos == null) {
            BrokerLogger.noEntryForSubscriber(subscriberId, topic);
            return;
        }

        subscribers.put(subscriberId, pos + 1);
        topicContentGarbageCollection(topic);

        sendAckMessage(recMessage.getTarget(), socket, ReplyMessages.receivedAcknowledgment(topic));
        onStateChanged();
    }

    private void handleSubMessage(RouterMessage subMessage, RouterSocket socket) {
        Subscribe sub;
        try {
            sub = new Subscribe(subMessage.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String topic = sub.getTopic();
        String subscriberId = sub.getSubscriberId();
        HashMap<String, Integer> subscribers = new HashMap<>();

        if (anySubscribed(topic)) {
            subscribers = topicSubscribers.get(topic);
            if (subscriberExistsInTopic(subscriberId, subscribers)) {
                sendNackMessage(subMessage.getTarget(), socket, ReplyMessages.alreadySubscribed(topic));
                return;
            }
        }

        subscribers.put(subscriberId, 0);
        topicSubscribers.put(topic, subscribers);
        sendAckMessage(subMessage.getTarget(), socket, ReplyMessages.subscribed(topic));
        onStateChanged();
    }

    private void handleUnsubMessage(RouterMessage unsubMessage, RouterSocket socket) {
        Unsubscribe unsub;
        try {
            unsub = new Unsubscribe(unsubMessage.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String topic = unsub.getTopic();
        String subscriberId = unsub.getSubscriberId();
        HashMap<String, Integer> subscribers;

        if (anySubscribed(topic)) {
            subscribers = topicSubscribers.get(topic);

            if (!subscriberExistsInTopic(subscriberId, subscribers)) {
                sendNackMessage(unsubMessage.getTarget(), socket, ReplyMessages.notSubscribed(topic));
            } else {
                subscribers.remove(subscriberId);
                topicSubscribers.put(topic, subscribers);

                topicSubscribersGarbageCollection(topic);

                sendAckMessage(unsubMessage.getTarget(), socket, ReplyMessages.unsubscribed(topic));
                onStateChanged();
            }
        } else {
            sendNackMessage(unsubMessage.getTarget(), socket, ReplyMessages.topicDoesNotExist(topic));
        }
    }

    private void handlePublisherMessage(RouterSocket socket) {
        var request = socket.receiveMessage();

        Put message;
        try {
            message = this.createPutMessage(request);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        SimpleEntry<String, Boolean> storedResult = this.storePutMessage(message);

        Action action;
        if (storedResult.getValue()) {
            action = Ack.createAck(storedResult.getKey());
            onStateChanged();
        }
        else action = Nack.createNack(storedResult.getKey());

        var reply = new RouterMessage(request.getTarget(), action);
        socket.sendMessage(reply);
    }

    /**
     * Create a put message from the received request
     *
     * @param request RouterMessage that was received on PUB port
     * @return Put Action with the parameters corresponding to the received message
     * @throws Exception Thrown in case an error occurred. Read error message for more information.
     */
    private Put createPutMessage(RouterMessage request) throws Exception {
        if (request.getContent().split(Messages.delimiter)[0].equals(Messages.put)) {
            return new Put(request.getContent());
        } else {
            throw new Exception("Unsupported message type " + request.getContent().split(Messages.delimiter)[0] + " from publisher.");
        }
    }

    /**
     * Stores the contents of the Put message received on the corresponding topic.
     * If the topic does not exist and at least 1 SUB subscribes it, creates the topic.
     * If no SUB subscribes the topic, does not store the information.
     *
     * @param message Put Action received from PUB socket.
     * @return SimpleEntry where key is return message and value states whether message was stores or not.
     */
    private SimpleEntry<String, Boolean> storePutMessage(Put message) {
        String topic = message.getTopic();
        String content = message.getContent();
        String reply = "";
        boolean stored = false;

        if (topicContents.containsKey(topic)) {
            topicContents.get(topic).add(content);
            reply = ReplyMessages.addedMessageToTopic(content, topic);
            stored = true;
        } else {
            if (anySubscribed(topic)) {
                // Create topic with message and add it to topics
                var topicList = new ArrayList<String>();
                topicList.add(content);
                topicContents.put(topic, topicList);

                reply = ReplyMessages.createdTopic(content, topic);
                stored = true;
            } else {
                reply = ReplyMessages.notStored(topic);
            }
        }

        return new SimpleEntry<>(reply, stored);
    }

    /**
     * Checks if there is, at least, 1 SUB subscribed to the topic
     *
     * @param topic String representing the topic
     * @return True if there is, false otherwise
     */
    private boolean anySubscribed(String topic) {
        return topicSubscribers.containsKey(topic) && topicSubscribers.get(topic).size() != 0;
    }

    private boolean subscriberExistsInTopic(String id, HashMap<String, Integer> subscribers) {
        return subscribers.containsKey(id);
    }

    private void topicSubscribersGarbageCollection(String topic) {
        // No SUBs are subbed to a topic
        if (topicSubscribers.get(topic).size() == 0) {
            topicSubscribers.remove(topic);
            topicContents.remove(topic);
        }
    }

    private void topicContentGarbageCollection(String topic) {
        var subscribers = topicSubscribers.get(topic);
        var minSubscriberEntry = subscribers.entrySet().stream()
            .min(Comparator.comparingInt(Map.Entry::getValue));

        if (minSubscriberEntry.isEmpty()) return;
        int minSubscriberPosition = minSubscriberEntry.get().getValue();

        if (minSubscriberPosition <= 0) return;

        for (var entry : subscribers.entrySet())
            entry.setValue(entry.getValue() - minSubscriberPosition);

        var contents = topicContents.get(topic);
        contents.subList(0, minSubscriberPosition).clear();
    }

    private void sendAckMessage(byte[] target, RouterSocket socket, String message) {
        Ack ack = Ack.createAck(message);
        sendReplyMessage(target, socket, ack);
        BrokerLogger.sentMessage(Messages.ack, ack.getContent());
    }

    private void sendNackMessage(byte[] target, RouterSocket socket, String message) {
        Nack nack = Nack.createNack(message);
        sendReplyMessage(target, socket, nack);
        BrokerLogger.sentMessage(Messages.nack, nack.getContent());
    }

    private void sendContentMessage(byte[] target, RouterSocket socket, String topic, String message) {
        Content content = new Content(topic, message);
        sendReplyMessage(target, socket, content);
        BrokerLogger.sentMessage(Messages.content, content.getContent());
    }

    private void sendReplyMessage(byte[] target, RouterSocket socket, Action action) {
        try {
            RouterMessage reply = new RouterMessage(target, action);
            socket.sendMessage(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean alwaysSaveState() {
        return executor == null;
    }

    private void onStateChanged() {
        if (alwaysSaveState()) saveState();
    }

    public void saveState() {
        try {
            File f = new File(topicSubscribersFileName);
            File f2 = new File(topicContentsFileName);
            if (f.exists()) {
                f.delete();
            }
            if (f2.exists()) {
                f2.delete();
            }

            FileOutputStream topicSubscribersFile = new FileOutputStream(topicSubscribersFileName);
            ObjectOutputStream file = new ObjectOutputStream(topicSubscribersFile);
            file.writeObject(topicSubscribers);
            file.close();

            FileOutputStream topicContentsFile = new FileOutputStream(topicContentsFileName);
            ObjectOutputStream file2 = new ObjectOutputStream(topicContentsFile);
            file2.writeObject(topicContents);
            file2.close();
        } catch(Exception e) {
            BrokerLogger.errorSavingState(e.getMessage());
        }
    }

    public void restoreState() throws IOException, ClassNotFoundException {
        File topicSubscribersFile = new File(topicSubscribersFileName);
        if (topicSubscribersFile.exists()) {
            FileInputStream fileInputStream = new FileInputStream(topicSubscribersFileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            topicSubscribers = (HashMap<String, HashMap<String, Integer>>) objectInputStream.readObject();
            objectInputStream.close();
            System.out.println("topicSubscribers restored with success");
        }

        File topicContentsFile = new File(topicContentsFileName);
        if (topicContentsFile.exists()) {
            FileInputStream fileInputStream = new FileInputStream(topicContentsFileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            topicContents = (HashMap<String, List<String>>) objectInputStream.readObject();
            objectInputStream.close();
            System.out.println("topicContents restored with success");
        }
    }

    private void connectToRmi() {
        String brokerId = "broker";
        try {
            BrokerInterface stub = (BrokerInterface) UnicastRemoteObject.exportObject(this, 0);
            try {
                LocateRegistry.createRegistry(1098);
                BrokerLogger.brokerCreatedRmi();
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
            Registry registry = LocateRegistry.getRegistry(1098);

            registry.rebind(brokerId, stub);
            BrokerLogger.brokerReady();
        } catch (Exception e) {
            BrokerLogger.rmiBrokerError(e);
            e.printStackTrace();
        }
    }

    @Override
    public String logDatabase() {
        StringBuilder log = new StringBuilder();

        log.append("[DATABASE] Topic Subscribers\n");
        if (topicSubscribers.isEmpty()) log.append("This data structure is empty\n");
        for (HashMap.Entry<String, HashMap<String, Integer>> entry : topicSubscribers.entrySet()) {
            log.append("\t# ").append(entry.getKey()).append(":\n");
            for (HashMap.Entry<String, Integer> pair : entry.getValue().entrySet()) {
                log.append("\t\t subscriberId: ").append(pair.getKey()).append("\t position: ").append(pair.getValue()).append("\n");
            }
        }

        log.append("[DATABASE] Topic Contents\n");
        if (topicContents.isEmpty()) log.append("This data structure is empty\n");
        for (HashMap.Entry<String, List<String>> entry : topicContents.entrySet()) {
            log.append("\t# ").append(entry.getKey()).append(":\n");
            List<String> items = entry.getValue();
            for (String item : items) {
                log.append("\t\t").append(item).append("\n");
            }
        }

        return log.toString();
    }
}
