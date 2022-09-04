package pt.up.fe.sdle.reliableps.loggers;

public class BrokerLogger {
    public static void noEntryForSubscriber(String id, String topic) {
        System.out.println("[LOG] No entry for (subscriber id = " + id + ") (topic = " + topic + ")");
    }

    public static void sentMessage(String message, String content) {
        System.out.println("[REPLY] Sent " + message  + " \"" + content + "\" message to SUB");
    }

    public static void errorStarting(String message) {
        System.err.println("[ERROR] Error while starting broker. " + message);
    }

    public static void errorSavingState(String message) {
        System.err.println("Error while saving broker state. " + message);
    }

    public static void brokerReady() {
        System.out.println("[RMI] Broker ready!");
    }

    public static void rmiBrokerError(Exception e) {
        System.out.println("[RMI] Error creating broker and connecting to RMI: " + e);
    }

    public static void brokerCreatedRmi() {
        System.out.println("[RMI] Created publisher connection!");
    }
}
