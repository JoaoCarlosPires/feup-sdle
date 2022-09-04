package pt.up.fe.sdle.reliableps.loggers;

public class SubscriberLogger {
    public static void subscriberError() {
        System.out.println("[ERROR] Execute with Subscriber <id> !");
    }

    public static void subscriberReady(String id) {
        System.out.println("[RMI] Subscriber with id = " + id + " ready!");
    }

    public static void rmiSubscriberError(Exception e) {
        System.out.println("[RMI] Error creating subscriber and connecting to RMI: " + e);
    }

    public static void subscriberCreatedRmi() {
        System.out.println("[RMI] Created subscriber connection!");
    }

    public static void subscriberJoinedRmi() {
        System.out.println("[RMI] Joined subscriber connection!");
    }
}
