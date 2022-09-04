package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Received extends SubscriberIdMessage {
    public Received(String topic, String subscriberId) {
        super(Messages.received, topic, subscriberId);
    }

    public Received(String message) throws Exception {
        super(message, Messages.received);
    }
}
