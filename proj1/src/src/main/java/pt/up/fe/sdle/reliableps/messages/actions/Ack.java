package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public class Ack extends ReplyMessage {
    public Ack() {
        super(Messages.ack);
    }

    public Ack(String message) throws Exception {
        super(message, Messages.ack);
    }

    public static Ack createAck(String message) {
        Ack ack = new Ack();
        ack.content = message;
        return ack;
    }
}
