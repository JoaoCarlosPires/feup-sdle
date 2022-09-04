package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Action {
    String action;

    public Action() {}
    public Action(String action) {
        this.action = action.split(Messages.delimiter, 2)[0];
    }

    public String getAction() {
        return action;
    }

    public String toString() {
        return action;
    }
}
