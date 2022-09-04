package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Put extends ContentMessage{
    public Put(String topic, String content) {
        super(Messages.put, topic, content);
    }

    public Put(String message) throws Exception {
        super(message, Messages.put);
    }
}
