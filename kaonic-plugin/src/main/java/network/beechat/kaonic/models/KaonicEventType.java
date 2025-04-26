package network.beechat.kaonic.models;


public interface KaonicEventType {
    /// messages
    String MESSAGE_TEXT = "text";
    String MESSAGE_LOCATION = "location";
    String MESSAGE_FILE_START = "file_start";
    String MESSAGE_FILE_CHUNK = "file_chunk";

    ///  calls
    String CALL_NEW = "new_call";
    String CALL_ANSWER = "call_answer";
    String CALL_REJECT = "call_reject";
    String CALL_VOICE = "call_voice";

    /// other

}
