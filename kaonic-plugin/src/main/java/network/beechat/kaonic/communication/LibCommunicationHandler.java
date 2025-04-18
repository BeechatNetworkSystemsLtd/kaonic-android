package network.beechat.kaonic.communication;

import androidx.annotation.NonNull;

import network.beechat.kaonic.libsource.KaonicDataChannelListener;
import network.beechat.kaonic.libsource.KaonicLib;

public class LibCommunicationHandler implements KaonicDataChannelListener {

    final private @NonNull KaonicLib kaonicLib;
    private DataPacketListener packetListener;

    public LibCommunicationHandler(@NonNull KaonicLib kaonicLib) {
        this.kaonicLib = kaonicLib;
        kaonicLib.setChannelListener(this);
    }

    public void onDestroy() {
        kaonicLib.removeChannelListener();
    }

    public void setPacketListener(DataPacketListener packetListener) {
        this.packetListener = packetListener;
    }

    public void removePacketListener() {
        this.packetListener = null;
    }

    @Override
    public void onDataReceive(byte[] bytes) {

    }
}
