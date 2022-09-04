package pt.up.fe.sdle.reliableps.testapp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SubscriberInterface extends Remote {
    String subscribe(String topic) throws RemoteException;
    String unsubscribe(String topic) throws RemoteException;
    String get(String topic) throws RemoteException;
    List<String> massGet(String topic, int amount) throws RemoteException;
}
