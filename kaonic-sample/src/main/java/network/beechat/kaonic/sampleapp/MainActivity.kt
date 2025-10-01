package network.beechat.kaonic.sampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import network.beechat.kaonic.communication.KaonicCommunicationManager
import network.beechat.kaonic.impl.KaonicLib
import network.beechat.kaonic.sampleapp.services.KaonicService
import network.beechat.kaonic.sampleapp.services.SecureStorageHelper
import network.beechat.kaonic.sampleapp.services.call.CallService
import network.beechat.kaonic.sampleapp.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val REQUEST_STORAGE_PERMISSION = 201
    }
    lateinit var secureStorageHelper: SecureStorageHelper
    lateinit var callService: CallService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callService = CallService(appScope)
        secureStorageHelper=SecureStorageHelper(this)
        setContent {
            SampleAppTheme {
                AppNavigator(callService,secureStorageHelper)
            }
        }

        checkAudioPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                initKaonicService()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                initKaonicService()
            }
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            initKaonicService()
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun initKaonicService() {
        checkStoragePermission()

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(this, ringtoneUri)

        KaonicService.init(
            KaonicCommunicationManager(
                KaonicLib.getInstance(applicationContext),
                contentResolver,
                ringtone, this
            ),
            secureStorageHelper
        )
    }
}