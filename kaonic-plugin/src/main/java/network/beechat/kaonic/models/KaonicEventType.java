package network.beechat.kaonic.models;

public interface KaonicEventType {
    /// messages
    String MESSAGE_TEXT = "Message";
    String MESSAGE_LOCATION = "location";
    String MESSAGE_FILE_START = "FileStart";
    String MESSAGE_FILE = "MessageFile";
    String MESSAGE_FILE_CHUNK = "MessageFile";
    String[] messageEvents = new String[]{MESSAGE_TEXT, MESSAGE_LOCATION, MESSAGE_FILE_START};

    ///  calls
    String CALL_NEW = "new_call";
    String CALL_ANSWER = "call_answer";
    String CALL_REJECT = "call_reject";
    String CALL_VOICE = "call_voice";
    String[] callEvents = new String[]{CALL_NEW, CALL_ANSWER, CALL_REJECT, CALL_VOICE};

    /// other
    String CONTACT_FOUND = "ContactFound";

}
