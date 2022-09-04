package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public abstract class ReplyMessage extends Action {
    String content;

    public ReplyMessage(String action) {
        super(action);
    }

    public ReplyMessage(String message, String target) throws Exception {
        String[] fields = message.split(Messages.delimiter, 2);
        action = fields[0];
        content = fields[1];

        if (!action.equals(target)) {
            System.out.println("Attempted to create " + target + " message with wrong identifier " + action);
            throw new Exception("Wrong message identifier!");
        }
    }

    public String toString() {
        return action + Messages.delimiter + content;
    }

    public String getContent() {
        return content;
    }
}
