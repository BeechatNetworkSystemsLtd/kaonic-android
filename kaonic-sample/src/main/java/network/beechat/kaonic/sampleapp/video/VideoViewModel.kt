package network.beechat.kaonic.sampleapp.video

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

class VideoViewModel : ViewModel() {

    fun startCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), { image: ImageProxy ->
                val frameData = imageToByteArray(image)

                image.close()
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    private fun imageToByteArray(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y
        yBuffer.get(nv21, 0, ySize)

        // NV21 format requires V and U to be interleaved as VU
        var uvPos = ySize
        val pixelStride = image.planes[1].pixelStride
        val rowStride = image.planes[1].rowStride
        val width = image.width
        val height = image.height

        val u = ByteArray(uSize)
        val v = ByteArray(vSize)
        uBuffer.get(u)
        vBuffer.get(v)

        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuIndex = row * rowStride + col * pixelStride
                nv21[uvPos++] = v[vuIndex]
                nv21[uvPos++] = u[vuIndex]
            }
        }

        return nv21
    }
}