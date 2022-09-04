package pt.up.fe.sdle.reliableps.messages;

import pt.up.fe.sdle.reliableps.messages.actions.Action;

public class RequestMessage extends Message {
    public RequestMessage(String content) {
        super(content);
    }
    public RequestMessage(Action action) { super(action); }
}
