package pt.up.fe.sdle.reliableps.constants;

public class ReplyMessages {
    public static String topicDoesNotExist(String topic) {
        return "Topic does not exist: " + topic;
    }

    public static String notSubscribed(String topic) {
        return "Not subscribed to topic: " + topic;
    }

    public static String alreadySubscribed(String topic) {
        return "Already subscribed to topic: " + topic;
    }

    public static String subscribed(String topic) {
        return "Successfully subscribed to topic: " + topic;
    }

    public static String unsubscribed(String topic) {
        return "Successfully unsubscribed to topic: " + topic;
    }

    public static String couldNotGetMessage(String topic) {
        return "Could not get message from topic: " + topic;
    }

    public static String receivedAcknowledgment(String topic) {
        return "Received acknowledgement from topic: " + topic;
    }

    public static String addedMessageToTopic(String content, String topic) {
        return "Added " + content + " to topic " + topic;
    }

    public static String createdTopic(String content, String topic) {
        return "Created topic " + content + " with " + topic;
    }

    public static String notStored(String topic) {
        return "Topic " + topic + " not stored because no subscribers are subscribed";
    }

    public static String buildLog(String action, String content) {
        return "[" + action + "] " + content;
    }
}
