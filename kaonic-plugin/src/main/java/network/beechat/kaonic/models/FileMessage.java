package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class FileMessage extends Message {
    final String fileName;
    private byte[] bytes;
    private String localPath;

    public FileMessage(String fileName, @NonNull Node sender, @NonNull Node recipient) {
        super(sender, recipient);
        this.fileName = fileName;
    }
}
