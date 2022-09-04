package pt.up.fe.sdle.reliableps.messages;

import pt.up.fe.sdle.reliableps.messages.actions.Action;

public abstract class Message {
    private final String content;

    Message(String content) {
        this.content = content;
    }

    Message (Action action) {
        this.content = action.toString();
    }

    public String getContent() {
        return content;
    }
}
