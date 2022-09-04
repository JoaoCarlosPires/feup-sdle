package pt.up.fe.sdle.reliableps.actions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zeromq.ZContext;
import pt.up.fe.sdle.reliableps.Broker;
import pt.up.fe.sdle.reliableps.Subscriber;
import pt.up.fe.sdle.reliableps.constants.Messages;
import pt.up.fe.sdle.reliableps.constants.ReplyMessages;

import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GetTest {
    static Broker broker;
    final String subscriberPort = "tcp://localhost:5001";

    public GetTest() {
        simulateSubForGetTest();
        simulatePutForGetTest();
    }

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
    public void testSubscribedGet() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "0");

        assert runGetTestInstance(subscriber, "football", ReplyMessages.buildLog(Messages.content, "Esforovite--"));
        assert runGetTestInstance(subscriber, "football", ReplyMessages.buildLog(Messages.content, "#oleout"));
        assert runGetTestInstance(subscriber, "football", ReplyMessages.buildLog(Messages.content, "Oliver Tsubasa assina pelo Benfica <3"));
        assert runGetTestInstance(subscriber, "f1", ReplyMessages.buildLog(Messages.content, "Hamilton e Max DNF no Qatar."));
        assert runGetTestInstance(subscriber, "f1", ReplyMessages.buildLog(Messages.content, "Lando Norris world champ in 2022"));
    }

    @Test
    public void testNotSubscribedGet() throws RemoteException {
        ZContext subscriberContext = new ZContext();
        Subscriber subscriber = new Subscriber(subscriberContext, subscriberPort, "1");

        assert runGetTestInstance(subscriber, "none", ReplyMessages.buildLog(Messages.nack, ReplyMessages.topicDoesNotExist("none")));
        assert runGetTestInstance(subscriber, "f1", ReplyMessages.buildLog(Messages.nack, ReplyMessages.notSubscribed("f1")));
    }

    private boolean runGetTestInstance(Subscriber subscriber, String topic, String expected) {
        subscriber.sendGetMessage(topic);
        broker.poll();
        SimpleEntry<String, Boolean> res = subscriber.receiveReply();
        if (res.getValue()) {
            subscriber.sendReceivedMessage(topic);
            broker.poll();
            subscriber.receiveReply();
        }
        return Objects.equals(res.getKey(), expected);
    }

    private void simulateSubForGetTest() {
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
        HashMap<String, List<String>> topicContents = new HashMap<>();

        ArrayList<String> football = new ArrayList<>();
        football.add("Esforovite--");
        football.add("#oleout");
        football.add("Oliver Tsubasa assina pelo Benfica <3");
        topicContents.put("football", football);

        ArrayList<String> f1 = new ArrayList<>();
        f1.add("Botjinhas é ídolo no Brasil.");
        f1.add("Hamilton e Max DNF no Qatar.");
        f1.add("Lando Norris world champ in 2022");
        f1.add("António Félix da Costa é o novo piloto da RedBull");
        topicContents.put("f1", f1);

        topicContents.put("bacalhaus", new ArrayList<>());

        broker.setTopicContents(topicContents);
    }
}
