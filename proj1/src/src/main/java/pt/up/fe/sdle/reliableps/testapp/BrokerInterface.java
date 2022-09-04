package pt.up.fe.sdle.reliableps.testapp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BrokerInterface extends Remote {
    String logDatabase() throws RemoteException;
}
