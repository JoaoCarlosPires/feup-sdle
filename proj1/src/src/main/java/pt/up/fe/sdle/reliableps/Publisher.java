package pt.up.fe.sdle.reliableps;

import org.zeromq.ZContext;
import pt.up.fe.sdle.reliableps.constants.Messages;
import pt.up.fe.sdle.reliableps.constants.ReplyMessages;
import pt.up.fe.sdle.reliableps.loggers.PublisherLogger;
import pt.up.fe.sdle.reliableps.messages.RequestMessage;
import pt.up.fe.sdle.reliableps.messages.actions.*;
import pt.up.fe.sdle.reliableps.sockets.ReqSocket;
import pt.up.fe.sdle.reliableps.testapp.PublisherInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;
import java.util.Random;

public class Publisher implements PublisherInterface {
    private final ReqSocket socket;
    private final String id;

    public Publisher(ZContext context, String port, String id) {
        this.socket = new ReqSocket(context, port);
        this.id = id;
    }

    public static void main(String[] args) {
        ZContext context = new ZContext();
        String port = "tcp://localhost:5002";
        if (args.length != 1) {
            PublisherLogger.publisherError();
            return;
        }
        String id = args[0];

        Publisher publisher = new Publisher(context, port, id);
        publisher.connectToRmi();
    }

    @Override
    public String put(String topic, String content) {
        try {
            sendPutMessage(topic, content);
            return receiveReply();
        } catch (Exception e) {
            e.printStackTrace();
            return "Put message not sent. " + e.getMessage();
        }
    }

    @Override
    public String massPut(String topic, int amount) {
        var random = new Random();
        try {
            for (int i = 0; i < amount; i++) {
                var content = String.valueOf(random.nextInt());
                sendPutMessage(topic, content);
                receiveReply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Put message not sent. " + e.getMessage();
        }
        return "Success.";
    }

    public void sendPutMessage(String topic, String content) {
        var action = new Put(topic, content);
        var request = new RequestMessage(action);
        this.socket.sendMessage(request);
    }

    private void connectToRmi() {
        try {
            PublisherInterface stub = (PublisherInterface) UnicastRemoteObject.exportObject(this, 0);
            try {
                LocateRegistry.createRegistry(1100);
                PublisherLogger.publisherCreatedRmi();
            } catch(Exception e) {
                PublisherLogger.publisherJoinedRmi();
            }
            Registry registry = LocateRegistry.getRegistry(1100);

            registry.rebind(id, stub);
            PublisherLogger.publisherReady(id);
        } catch (Exception e) {
            PublisherLogger.rmiPublisherError(e);
            e.printStackTrace();
        }
    }

    public String receiveReply() {
        RequestMessage reply =  this.socket.receiveMessage();

        String replyMessage = reply.getContent();

        Action action = new Action(replyMessage);
        try {
            return buildLog(action, replyMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String buildLog(Action action, String message) throws Exception {
        String actionName = action.getAction();
        String logContent = "Invalid action!";

        if (Objects.equals(actionName, Messages.ack)) {
            Ack ack = new Ack(message);
            logContent = ack.getContent();
        }
        else if (Objects.equals(actionName, Messages.nack)) {
            Nack nack = new Nack(message);
            logContent = nack.getContent();
        }
        return ReplyMessages.buildLog(actionName, logContent);
    }
}
