package pt.up.fe.sdle.reliableps.messages.actions;

public abstract class SubscriberIdMessage extends ThreeParamMessage {

    public SubscriberIdMessage(String action, String topic, String subscriberId) {
        super(action, topic, subscriberId);
    }

    protected SubscriberIdMessage(String message, String target) throws Exception {
        super(message, target);
    }

    public String getSubscriberId() {
        return thirdParam;
    }
}
