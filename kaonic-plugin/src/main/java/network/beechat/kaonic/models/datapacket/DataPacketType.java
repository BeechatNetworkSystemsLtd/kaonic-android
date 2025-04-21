package network.beechat.kaonic.models.datapacket;

// announce is a callback
public interface DataPacketType {
    public static final int TEXT_MESSAGE = 1;
    public static final int LOCATION_MESSAGE = 2;
    public static final int FILE_START_MESSAGE = 3;
    public static final int FILE_CHUNK_MESSAGE = 4;
}
