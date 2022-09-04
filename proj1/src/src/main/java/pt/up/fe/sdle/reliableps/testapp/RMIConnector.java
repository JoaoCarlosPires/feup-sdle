package pt.up.fe.sdle.reliableps.testapp;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIConnector {
    public static <T extends Remote> T getStub(String lookupName, int registryPort) {
        try {
            Registry registry = LocateRegistry.getRegistry(registryPort);
            T aux = (T) registry.lookup(lookupName);
            System.out.println("[RMI] Connected!");
            return aux;
        } catch (Exception e) {
            System.err.println("[RMI] Error connecting to RMI: " + e.getMessage());
        }
        return null;
    }
    public static <T extends Remote> T getStub(String lookupName) {
        return RMIConnector.getStub(lookupName, 1099);
    }
}
