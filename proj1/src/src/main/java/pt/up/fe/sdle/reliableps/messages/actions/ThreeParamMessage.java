package pt.up.fe.sdle.reliableps.messages.actions;

import pt.up.fe.sdle.reliableps.constants.Messages;

public abstract class ThreeParamMessage extends Action {
    String topic;
    String thirdParam;

    public ThreeParamMessage(String action, String topic, String subscriberId) {
        super(action);
        this.topic = topic;
        this.thirdParam = subscriberId;
    }

    protected ThreeParamMessage(String message, String target) throws Exception {
        String[] fields = message.split(Messages.delimiter, 3);
        this.action = fields[0];
        this.topic = fields[1];
        this.thirdParam = fields[2];

        if (!action.equals(target)) {
            System.out.println("Attempted to create " + target + " message with wrong identifier " + action);
            throw new Exception("Wrong message identifier!");
        }
    }

    public String getTopic() {
        return topic;
    }

    public String toString() {
        return action + Messages.delimiter + topic + Messages.delimiter + thirdParam;
    }
}
