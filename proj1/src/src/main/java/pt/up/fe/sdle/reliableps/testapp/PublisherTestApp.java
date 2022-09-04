package pt.up.fe.sdle.reliableps.testapp;

import pt.up.fe.sdle.reliableps.constants.Messages;

import java.util.Objects;

public class PublisherTestApp {
    private PublisherInterface stub;
    private String id, topic, content, action;

    public static void main(String[] args) throws Exception {
        PublisherTestApp testApp = new PublisherTestApp();
        if (!testApp.parseArgs(args)) return;
        if (!testApp.connectRmi()) return;
        System.out.println(testApp.processRequest());
    }

    private String processRequest() throws Exception {
        if (Objects.equals(action, Messages.put)) return stub.put(this.topic, this.content);
        if (Objects.equals(action, "MASSPUT")) return stub.massPut(this.topic, Integer.parseInt(this.content));

        throw new Exception("Received unknown action type '" + action + "'.");
    }

    private boolean parseArgs(String[] args) {
        if (args.length == 1) args = args[0].split(" ");

        this.action = args.length == 4 ? args[1].toUpperCase() : null;
        if (args.length != 4 || (!Objects.equals(this.action, Messages.put) && !Objects.equals(this.action, "MASSPUT"))) {
            System.out.println("Usage: <publisherId> <action: 'PUT' | 'MASSPUT'> <topic> <content|amount>");
            return false;
        }
        this.id = args[0];
        this.topic = args[2];
        this.content = args[3];
        return true;
    }

    private boolean connectRmi() {
        this.stub = RMIConnector.getStub(this.id, 1100);
        return this.stub != null;
    }
}
