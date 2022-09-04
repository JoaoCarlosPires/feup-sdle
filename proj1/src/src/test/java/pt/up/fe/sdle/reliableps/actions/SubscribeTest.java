package pt.up.fe.sdle.reliableps.actions;

import org.junit.jupiter.api.*;
import org.zeromq.ZContext;
import pt.up.fe.sdle.reliableps.Broker;
import pt.up.fe.sdle.reliableps.Subscriber;
import pt.up.fe.sdle.reliableps.constants.Messages;
import pt.up.fe.sdle.reliableps.constants.ReplyMessages;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubscribeTest {
    static Broker broker;
    final String subscriberPort = "tcp://localhost:5001";

    @BeforeAll
    public static void start() throws Exception {
        ZContext context = new ZContext();
        broker = new Broker(context, "tcp://*:5001", "tcp://*:5002", false);
    }

    @AfterAll
    public static void kill() {
        broker.kill();
    }

    @Test
    @Order(1)
    public void testSubscribe() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "0");

        String topic = "football";
        assert runSubscribeTestInstance(subscriber, topic, ReplyMessages.buildLog(Messages.ack, ReplyMessages.subscribed(topic)));
    }

    @Test
    @Order(2)
    public void testUnsubscribe() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "0");

        String topic = "football";
        subscriber.sendSubscribeMessage(topic);
        broker.poll();
        subscriber.receiveReply();

        assert runUnsubscribeTestInstance(subscriber, topic, ReplyMessages.buildLog(Messages.ack, ReplyMessages.unsubscribed(topic)));
    }

    @Test
    @Order(3)
    public void testUnsubscribeNoTopic() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "0");

        String topic = "123";
        assert runUnsubscribeTestInstance(subscriber, topic, ReplyMessages.buildLog(Messages.nack, ReplyMessages.topicDoesNotExist(topic)));
    }

    @Test
    @Order(4)
    public void testSubscribeAlreadySubscribed() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "0");

        String topic = "123";
        subscriber.sendSubscribeMessage(topic);
        broker.poll();
        subscriber.receiveReply();

        assert runSubscribeTestInstance(subscriber, topic, ReplyMessages.buildLog(Messages.nack, ReplyMessages.alreadySubscribed(topic)));
    }

    @Test
    @Order(5)
    public void testUnsubscribeNotSubscribed() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "0");
        Subscriber subscriber1 = new Subscriber(subscriberContext, subscriberPort, "1");

        String topic = "123";
        subscriber.sendSubscribeMessage(topic);
        broker.poll();
        subscriber.receiveReply();

        assert runUnsubscribeTestInstance(subscriber1, topic, ReplyMessages.buildLog(Messages.nack, ReplyMessages.notSubscribed(topic)));
    }

    private boolean runSubscribeTestInstance(Subscriber subscriber, String topic, String expected) {
        subscriber.sendSubscribeMessage(topic);
        broker.poll();
        String res = subscriber.receiveReply().getKey();
        return Objects.equals(res, expected);
    }

    private boolean runUnsubscribeTestInstance(Subscriber subscriber, String topic, String expected) {
        subscriber.sendUnsubscribeMessage(topic);
        broker.poll();
        String res = subscriber.receiveReply().getKey();
        return Objects.equals(res, expected);
    }

    private void simulateSubForPutTest() {
        HashMap<String, HashMap<String, Integer>> topicSubscribers = broker.getTopicSubscribers();

        HashMap<String, Integer> football = new HashMap<>();
        football.put("0", 0);
        HashMap<String, Integer> f1 = new HashMap<>();
        f1.put("0", 1);
        topicSubscribers.put("football", football);
        topicSubscribers.put("f1", f1);

        broker.setTopicSubscribers(topicSubscribers);
    }
}
