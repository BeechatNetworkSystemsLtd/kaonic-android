package network.beechat.kaonic.communication.factory;

import androidx.annotation.NonNull;

import network.beechat.kaonic.communication.base.KaonicBaseManager;
import network.beechat.kaonic.impl.KaonicLib;

public class KaonicFactoryCheckManager extends KaonicBaseManager {
    final private String TAG = "KaonicFactoryCheckService";

    public KaonicFactoryCheckManager(@NonNull KaonicLib kaonicLib){
        super(kaonicLib);
    }
}
