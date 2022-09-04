package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Nack extends ReplyMessage {
    public Nack() {
        super(Messages.nack);
    }

    public Nack(String message) throws Exception {
        super(message, Messages.nack);
    }

    public static Nack createNack(String message) {
        Nack nack = new Nack();
        nack.content = message;
        return nack;
    }
}
