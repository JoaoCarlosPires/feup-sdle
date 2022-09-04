package pt.up.fe.sdle.reliableps.testapp;

import java.rmi.RemoteException;

public class BrokerTestApp {
    private BrokerInterface stub;

    public static void main(String[] args) throws RemoteException {
        BrokerTestApp testApp = new BrokerTestApp();
        if (!testApp.connectRmi()) return;
        System.out.println(testApp.processRequest());
    }

    private String processRequest() throws RemoteException {
        return stub.logDatabase();
    }

    private boolean connectRmi() {
        String brokerId = "broker";
        this.stub = RMIConnector.getStub(brokerId, 1098);
        return this.stub != null;
    }
}
