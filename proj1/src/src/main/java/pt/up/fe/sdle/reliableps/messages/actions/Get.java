package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Get extends SubscriberIdMessage {
    public Get(String topic, String subscriberId) {
        super(Messages.get, topic, subscriberId);
    }

    public Get(String message) throws Exception {
        super(message, Messages.get);
    }
}
