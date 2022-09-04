package pt.up.fe.sdle.reliableps.messages;

import pt.up.fe.sdle.reliableps.messages.actions.Action;
import pt.up.fe.sdle.reliableps.messages.actions.Get;
import pt.up.fe.sdle.reliableps.messages.actions.Put;
import org.junit.jupiter.api.Test;
import pt.up.fe.sdle.reliableps.constants.Messages;

import java.util.Objects;

public class ActionTest {
    @Test
    public void buildGet() throws Exception {
        String topic = "weather";
        String id = "2";
        String s = Messages.get + Messages.delimiter + topic + Messages.delimiter + id;
        Get get1 = new Get(topic, id);
        Get get2 = new Get(s);

        assert Objects.equals(get1.getAction(), get2.getAction());
        assert Objects.equals(get1.getTopic(), get2.getTopic());
        assert Objects.equals(get1.getSubscriberId(), get2.getSubscriberId());

        assert Objects.equals(get1.getAction(), Messages.get);
        assert Objects.equals(get1.getTopic(), topic);
        assert Objects.equals(get1.getSubscriberId(), id);
    }

    @Test
    public void buildPut() throws Exception {
        String topic = "weather";
        String content = "100F sunny";
        String s = Messages.put + Messages.delimiter + topic + Messages.delimiter + content;
        Put put1 = new Put(topic, content);
        Put put2 = new Put(s);

        assert Objects.equals(put1.getAction(), put2.getAction());
        assert Objects.equals(put1.getTopic(), put2.getTopic());
        assert Objects.equals(put1.getContent(), put2.getContent());

        assert Objects.equals(put1.getAction(), Messages.put);
        assert Objects.equals(put1.getTopic(), topic);
        assert Objects.equals(put1.getContent(), content);
    }

    @Test
    public void receivedMessage() {
        String topic = "weather";
        String content = "100F sunny";
        String s = Messages.put + Messages.delimiter + topic + Messages.delimiter + content;

        Action a = new Action(s);

        assert Objects.equals(a.getAction(), Messages.put);

        try {
            new Get(s);
            assert false;
        } catch (Exception e) {
            assert true;
        }

        try {
            new Put(s);
            assert true;
        } catch (Exception e) {
            assert false;
        }
    }

    @Test
    public void toStringTest() throws Exception {
        String topic = "weather";
        String content = "100F sunny";
        String s = Messages.put + Messages.delimiter + topic + Messages.delimiter + content;
        assert new Put(s).toString().equals(s);
    }
}
