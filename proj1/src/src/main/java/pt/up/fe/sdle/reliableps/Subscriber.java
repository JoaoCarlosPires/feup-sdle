package pt.up.fe.sdle.reliableps;

import org.zeromq.ZContext;
import pt.up.fe.sdle.reliableps.constants.ReplyMessages;
import pt.up.fe.sdle.reliableps.loggers.SubscriberLogger;
import pt.up.fe.sdle.reliableps.constants.Messages;
import pt.up.fe.sdle.reliableps.messages.RequestMessage;
import pt.up.fe.sdle.reliableps.messages.actions.*;
import pt.up.fe.sdle.reliableps.sockets.ReqSocket;
import pt.up.fe.sdle.reliableps.testapp.SubscriberInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Subscriber implements SubscriberInterface {
    private final ReqSocket socket;
    private final String id;

    public Subscriber(ZContext context, String port, String id) throws RemoteException {
        this.socket = new ReqSocket(context, port);
        this.id = id;
    }

    public static void main(String[] args) throws RemoteException {
        ZContext context = new ZContext();
        String port = "tcp://localhost:5001";
        if (args.length != 1) {
            SubscriberLogger.subscriberError();
            return;
        }
        String id = args[0];

        Subscriber subscriber;
        try {
            subscriber = new Subscriber(context, port, id);
        } catch(Exception e) {
            System.out.println("[ERROR] Instantiating subscriber...");
            return;
        }
        subscriber.connectToRmi();
    }

    @Override
    public String subscribe(String topic) {
        sendSubscribeMessage(topic);
        return receiveReply().getKey();
    }

    @Override
    public String unsubscribe(String topic) {
        sendUnsubscribeMessage(topic);
        return receiveReply().getKey();
    }

    @Override
    public String get(String topic) {
        sendGetMessage(topic);
        SimpleEntry<String, Boolean> reply = receiveReply();
        if (reply.getValue()) {
            sendReceivedMessage(topic);
            receiveReply();
        }
        return reply.getKey();
    }

    @Override
    public List<String> massGet(String topic, int amount) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            sendGetMessage(topic);
            SimpleEntry<String, Boolean> reply = receiveReply();
            if (reply.getValue()) {
                sendReceivedMessage(topic);
                receiveReply();
            }
            if (!reply.getValue()) continue;
            result.add(reply.getKey());
        }
        return result;
    }

    public void sendGetMessage(String topic) {
        Get get = new Get(topic, id);
        sendAction(get);
    }

    public void sendUnsubscribeMessage(String topic) {
        Unsubscribe unsubscribe = new Unsubscribe(topic, id);
        sendAction(unsubscribe);
    }

    public void sendSubscribeMessage(String topic) {
        Subscribe subscribe = new Subscribe(topic, id);
        sendAction(subscribe);
    }

    public void sendReceivedMessage(String topic) {
        Received received = new Received(topic, id);
        sendAction(received);
    }

    public SimpleEntry<String, Boolean> receiveReply() {
        RequestMessage reply = socket.receiveMessage();
        String replyMessage = reply.getContent();

        Action action = new Action(replyMessage);
        try {
            return buildLog(action, replyMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendAction(Action action) {
        var request = new RequestMessage(action);
        this.socket.sendMessage(request);
    }

    private SimpleEntry<String, Boolean> buildLog(Action action, String message) throws Exception {
        String actionName = action.getAction();
        String logContent = "Invalid action!";
        boolean shouldReply = false;

        if (Objects.equals(actionName, Messages.ack)) {
            Ack ack = new Ack(message);
            logContent = ack.getContent();
        }
        else if (Objects.equals(actionName, Messages.nack)) {
            Nack nack = new Nack(message);
            logContent = nack.getContent();
        }
        else if (Objects.equals(actionName, Messages.content)) {
            Content content = new Content(message);
            logContent = content.getContent();
            shouldReply = true;
        }
        return new SimpleEntry<>(ReplyMessages.buildLog(actionName, logContent), shouldReply);
    }

    private void connectToRmi() {
        try {
            SubscriberInterface stub = (SubscriberInterface) UnicastRemoteObject.exportObject(this, 0);

            try {
                LocateRegistry.createRegistry(1099);
                SubscriberLogger.subscriberCreatedRmi();
            } catch(Exception e) {
                SubscriberLogger.subscriberJoinedRmi();
            }
            Registry registry = LocateRegistry.getRegistry(1099);

            registry.rebind(id, stub);
            SubscriberLogger.subscriberReady(id);

        } catch (Exception e) {
            SubscriberLogger.rmiSubscriberError(e);
            e.printStackTrace();
        }
    }
}
