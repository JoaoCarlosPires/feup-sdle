package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Content extends ContentMessage {
    public Content(String topic, String content) {
        super(Messages.content, topic, content);
    }

    public Content(String message) throws Exception {
        super(message, Messages.content);
    }
}