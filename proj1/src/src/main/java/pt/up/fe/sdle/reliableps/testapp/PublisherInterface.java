package pt.up.fe.sdle.reliableps.testapp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PublisherInterface extends Remote {
    String put(String topic, String content) throws RemoteException;
    String massPut(String topic, int amount) throws RemoteException;
}
