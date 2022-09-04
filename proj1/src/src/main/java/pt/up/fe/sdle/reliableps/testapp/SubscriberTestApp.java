package pt.up.fe.sdle.reliableps.testapp;

import pt.up.fe.sdle.reliableps.constants.Messages;
import java.util.Objects;

public class SubscriberTestApp {
    private SubscriberInterface stub;
    private String action, topic, id;
    private int amount;

    public static void main(String[] args) throws Exception {
        SubscriberTestApp testApp = new SubscriberTestApp();
        if (!testApp.parseArgs(args)) return;
        if (!testApp.connectRmi()) return;
        System.out.println(testApp.processRequest());
    }

    private boolean parseArgs(String[] args) {
        if (args.length == 1) args = args[0].split(" ");
        boolean isRightArgC = args.length == 3 || args.length == 4;

        this.action = isRightArgC ? args[1].toUpperCase() : null;
        boolean actionExists = this.action != null && (this.action.equals(Messages.sub)
                || this.action.equals(Messages.unsub) || this.action.equals(Messages.get) || this.action.equals("MASSGET"));

        if (!(isRightArgC && actionExists)) {
            System.out.println("Usage: <subscriberId> <action: 'GET' | 'SUBSCRIBE' | 'UNSUBSCRIBE' | 'MASSGET'> <topic> [amount (only for MASSGET)]");
            return false;
        }

        this.id = args[0];
        this.topic = args[2];
        this.amount = args.length == 4 ? Integer.parseInt(args[3]) : -1;
        return true;
    }

    private boolean connectRmi() {
        this.stub = RMIConnector.getStub(this.id);
        return this.stub != null;
    }

    private String processRequest() throws Exception {
        if (Objects.equals(action, Messages.get)) return stub.get(topic);
        if (Objects.equals(action, Messages.sub)) return stub.subscribe(topic);
        if (Objects.equals(action, Messages.unsub)) return stub.unsubscribe(topic);
        if (Objects.equals(action, "MASSGET")) {
            var result = stub.massGet(topic, amount);
            return String.join("\n", result);
        }

        throw new Exception("Received unknown action type '" + action + "'.");
    }
}
