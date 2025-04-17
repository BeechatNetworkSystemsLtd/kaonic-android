package network.beechat.kaonic.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager

class UsbService(private val usbManager: UsbManager) : SerialInputOutputManager.Listener {
    private val writeTimeout = 10000
    private val readTimeout = 20000
    var connection: UsbDeviceConnection? = null
    var port: UsbSerialPort? = null
    var listener: SerialInputOutputManager.Listener? = null

    fun getUsbPorts(): List<UsbSerialPort> {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val ports = arrayListOf<UsbSerialPort>()
        drivers.forEach {
            ports.addAll(it.ports)
        }

        return ports
    }

    fun openPort(deviceName: String, portNumber: Int): UsbDevice? {
        if (connection != null) {
            connection?.close()
        }
        if (port != null) {
            port!!.close()
        }

        val ports = getUsbPorts();
        val port = ports.firstOrNull {
            it.device.deviceName == deviceName && it.portNumber == portNumber
        }
        if (port == null) throw Exception("No device found with name $deviceName")
        val connection: UsbDeviceConnection = usbManager.openDevice(port.device)
            ?: return port.device// if null - add UsbManager.requestPermission(driver.getDevice(), ..)

        this.port = port
        this.connection = connection
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        return null
    }

    fun closeAllPorts() {
        if (connection != null) {
            connection?.close()
            connection = null
        }
        if (port != null) {
            port!!.close()
            port = null
        }
    }

    fun writeToPort(bytes: ByteArray) {
        if (port == null || !port!!.isOpen) return

        port!!.write(bytes, writeTimeout)
    }

    fun read(bytes: ByteArray) {
        if (port == null || !port!!.isOpen) return

        port!!.read(bytes, readTimeout)
    }

    fun startListenPortData(listener: SerialInputOutputManager.Listener) {
        if (port == null || !port!!.isOpen) return
        this.listener = listener
        SerialInputOutputManager(port!!, this)
    }

    override fun onNewData(data: ByteArray?) {
        listener?.onNewData(data)
    }

    override fun onRunError(e: java.lang.Exception?) {
        listener?.onRunError(e)
    }
}