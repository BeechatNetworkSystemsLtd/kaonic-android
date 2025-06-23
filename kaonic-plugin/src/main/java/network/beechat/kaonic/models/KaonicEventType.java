package network.beechat.kaonic.models;

import androidx.annotation.Keep;

@Keep
public interface KaonicEventType {
    /// messages
    String CHAT_CREATE = "ChatCreate";
    String MESSAGE_TEXT = "Message";
    String MESSAGE_LOCATION = "location";
    String MESSAGE_FILE_START = "FileStart";
    String MESSAGE_FILE = "MessageFile";
    String[] messageEvents = new String[]{MESSAGE_TEXT, MESSAGE_LOCATION, MESSAGE_FILE,
            CHAT_CREATE};

    ///  calls
    String CALL_INVOKE = "CallInvoke";
    String CALL_ANSWER = "CallAnswer";
    String CALL_REJECT = "CallReject";
    String CALL_AUDIO = "CallAudio";
    String CALL_TIMEOUT = "CallTimeout";
    String[] callEvents = new String[]{CALL_INVOKE, CALL_ANSWER,
            CALL_REJECT, CALL_TIMEOUT, CALL_AUDIO};

    /// other
    String CONTACT_FOUND = "ContactFound";
    String BROADCAST = "Broadcast";

}
