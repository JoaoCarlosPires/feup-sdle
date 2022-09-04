package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Unsubscribe extends SubscriberIdMessage {
    public Unsubscribe(String topic, String subscriberId) {
        super(Messages.unsub, topic, subscriberId);
    }

    public Unsubscribe(String message) throws Exception {
        super(message, Messages.unsub);
    }
}
