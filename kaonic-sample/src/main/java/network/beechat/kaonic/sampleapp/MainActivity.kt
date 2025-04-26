package network.beechat.kaonic.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import network.beechat.kaonic.sampleapp.theme.SampleAppTheme
import network.beechat.kaonic.storage.SecureStorageHelper

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleAppTheme {
                AppNavigator()
            }
        }


        val secureStorageHelper= SecureStorageHelper(this)
        secureStorageHelper.put("DDDD","askfdfkjndfkf");
        val result = secureStorageHelper.get("DDDD")
        print("")
    }
}