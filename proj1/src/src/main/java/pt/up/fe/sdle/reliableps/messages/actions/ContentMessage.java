package pt.up.fe.sdle.reliableps.messages.actions;

public abstract class ContentMessage extends ThreeParamMessage {

    public ContentMessage(String action, String topic, String content) {
        super(action, topic, content);
    }

    protected ContentMessage(String message, String target) throws Exception {
        super(message, target);
    }

    public String getContent() {
        return thirdParam;
    }
}
