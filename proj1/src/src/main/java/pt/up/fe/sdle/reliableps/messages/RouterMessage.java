package pt.up.fe.sdle.reliableps.messages;

import pt.up.fe.sdle.reliableps.messages.actions.Action;

public class RouterMessage extends Message {
    private final byte[] target;

    public RouterMessage(byte[] target, String content) {
        super(content);
        this.target = target;
    }

    public RouterMessage(byte[] target, Action action) {
        super(action);
        this.target = target;
    }

    public byte[] getTarget() {
        return target;
    }
}
