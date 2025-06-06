package network.beechat.kaonic.communication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class FileManager {
    protected Uri fileUri;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected boolean initialized = false;
    protected int fileSize = 0;
    protected int processedBytes = 0;
    protected String fileName;
    protected String fileId;
    protected String chatId;
    protected String address;

    public void startWrite(ContentResolver resolver, String fileName, int fileSize, String fileId, String chatId, String address) throws IOException {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.chatId = chatId;
        this.fileId = fileId;
        this.address = address;
        close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            if (fileUri != null) {
                outputStream = resolver.openOutputStream(fileUri, "wa");
                initialized = true;
            }
        } else {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!path.exists()) path.mkdirs();
            File file = new File(path, fileName);
            if(!file.exists()) file.createNewFile();
            fileUri = Uri.fromFile(file);
            outputStream = new FileOutputStream(file, true);
            initialized = true;
        }

    }

    /// return true if the file write operation if finished
    public boolean writeChunk(byte[] chunk) {
        if (!initialized || outputStream == null) return true;

        try {
            outputStream.write(chunk);
            outputStream.flush(); // optional: call less often for better performance
            processedBytes = processedBytes + chunk.length;
            return processedBytes >= fileSize;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean startSend(ContentResolver resolver, String fileId, String chatId, String address, String fileUriString) throws FileNotFoundException {
        close();
        this.chatId = chatId;
        this.fileId = fileId;
        this.address = address;
        this.fileUri = Uri.parse(fileUriString);

        Cursor cursor = resolver.query(this.fileUri, null, null, null, null);
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst() && sizeIndex != -1 && nameIndex != -1) {
                fileSize = (int) cursor.getLong(sizeIndex);
                fileName = cursor.getString(nameIndex);
            } else {
                return false;
            }
        }

        inputStream = resolver.openInputStream(this.fileUri);
        initialized = true;

        return true;
    }


    /// return true when all chunks are sent the file write operation if finished
    public byte[] nextChunk(int chunkSize) throws IOException {
        if (!initialized || inputStream == null || isFinished()) return new byte[]{};

        byte[] chunk = new byte[chunkSize];

        final int readCount = inputStream.read(chunk, 0, chunkSize);
        processedBytes += readCount;

        if (readCount < chunkSize) {
            chunk = Arrays.copyOf(chunk, readCount);
        }

        return chunk;
    }

    public boolean isFinished() {
        return processedBytes >= fileSize;
    }

    public void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ignored) {
        }
        outputStream = null;
        inputStream = null;
        fileUri = null;
        initialized = false;

    }


    public int getFileSize() {
        return fileSize;
    }

    public int getProcessedBytes() {
        return processedBytes;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public String getFileId() {
        return fileId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getAddress() {
        return address;
    }

    public String getFileName() {
        return fileName;
    }
}
