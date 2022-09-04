package pt.up.fe.sdle.reliableps.actions;

import org.junit.jupiter.api.*;
import org.zeromq.ZContext;
import pt.up.fe.sdle.reliableps.Broker;
import pt.up.fe.sdle.reliableps.Publisher;
import pt.up.fe.sdle.reliableps.constants.Messages;
import pt.up.fe.sdle.reliableps.constants.ReplyMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PutTest {
    static Broker broker;
    final String publisherPort = "tcp://localhost:5002";

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
    public void testNoSubscribers() {
        ZContext subscriberContext = new ZContext();
        Publisher publisher = new Publisher(subscriberContext, publisherPort, "0");

        String content = "New message";
        String topic = "football";
        assert runPutTestInstance(publisher, topic, content, ReplyMessages.buildLog(Messages.nack, ReplyMessages.notStored(topic)));
    }

    @Test
    @Order(2)
    public void testCreatedMessage() {
        ZContext subscriberContext = new ZContext();
        Publisher publisher = new Publisher(subscriberContext, publisherPort, "0");
        simulateSubForPutTest();

        String content = "New message";
        String topic = "football";
        assert runPutTestInstance(publisher, topic, content, ReplyMessages.buildLog(Messages.ack, ReplyMessages.createdTopic(content, topic)));
    }

    @Test
    @Order(3)
    public void testAddedMessage() {
        ZContext subscriberContext = new ZContext();
        Publisher publisher = new Publisher(subscriberContext, publisherPort, "0");
        simulatePutForGetTest();

        String content = "New message";
        String topic = "football";
        assert runPutTestInstance(publisher, topic, content, ReplyMessages.buildLog(Messages.ack, ReplyMessages.addedMessageToTopic(content, topic)));
    }

    private boolean runPutTestInstance(Publisher publisher, String topic, String content, String expected) {
        publisher.sendPutMessage(topic, content);
        broker.poll();
        String res = publisher.receiveReply();
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

    private void simulatePutForGetTest() {
        HashMap<String, List<String>> pubItems = new HashMap<>();

        ArrayList<String> football = new ArrayList<>();
        football.add("Esforovite--");
        football.add("#oleout");
        football.add("Oliver Tsubasa assina pelo Benfica <3");
        pubItems.put("football", football);

        ArrayList<String> f1 = new ArrayList<>();
        f1.add("Botjinhas é ídolo no Brasil.");
        f1.add("Hamilton e Max DNF no Qatar.");
        f1.add("Lando Norris world champ in 2022");
        f1.add("António Félix da Costa é o novo piloto da RedBull");
        pubItems.put("f1", f1);

        pubItems.put("bacalhaus", new ArrayList<>());

        broker.setTopicContents(pubItems);
    }
}
