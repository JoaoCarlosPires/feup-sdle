package pt.up.fe.sdle.reliableps.loggers;

public class PublisherLogger {
    public static void publisherError() {
        System.out.println("[ERROR] Execute with Publisher <id> !");
    }

    public static void publisherReady(String id) {
        System.out.println("[RMI] Publisher with id = " + id + " ready!");
    }

    public static void rmiPublisherError(Exception e) {
        System.out.println("[RMI] Error creating publisher and connecting to RMI: " + e);
    }

    public static void publisherCreatedRmi() {
        System.out.println("[RMI] Created publisher connection!");
    }

    public static void publisherJoinedRmi() {
        System.out.println("[RMI] Joined publisher connection!");
    }
}
