package network.beechat.kaonic;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileWriteHelper {
    private Uri fileUri;
    private OutputStream outputStream;
    private boolean initialized = false;
    private int fileSize = 0;
    private int writtenSize = 0;

    public void open(ContentResolver resolver, String fileName, int fileSize) {
        this.fileSize = fileSize;
        this.writtenSize = 0;
        close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (fileUri != null) {
                    outputStream = resolver.openOutputStream(fileUri, "wa"); // "wa" = write + append
                    initialized = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists()) path.mkdirs();
                File file = new File(path, fileName);
                outputStream = new FileOutputStream(file, true); // append mode
                initialized = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /// return true if the file write operation if finished
    public boolean writeChunk(byte[] chunk) {
        if (!initialized || outputStream == null) return true;

        try {
            outputStream.write(chunk);
            outputStream.flush(); // optional: call less often for better performance
            writtenSize = writtenSize + chunk.length;
            return writtenSize >= fileSize;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void close() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException ignored) {
            }
        }
        outputStream = null;
        fileUri = null;
        initialized = false;

    }

    public int getFileSize() {
        return fileSize;
    }

    public Uri getFileUri() {
        return fileUri;
    }
}