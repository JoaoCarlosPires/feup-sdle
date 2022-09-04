package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Subscribe extends SubscriberIdMessage {
    public Subscribe(String topic, String subscriberId) {
        super(Messages.sub, topic, subscriberId);
    }

    public Subscribe(String message) throws Exception {
        super(message, Messages.sub);
    }
}
