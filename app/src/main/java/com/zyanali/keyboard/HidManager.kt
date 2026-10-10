package com.zyanali.keyboard

import android.bluetooth.*
import android.content.Context
import android.os.ParcelUuid
import java.util.concurrent.Executors

class HidManager(private val context: Context) {

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    var statusCallback: ((String) -> Unit)? = null
    private var modifierState: Byte = 0

    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        val HID_SERVICE_UUID: ParcelUuid =
            ParcelUuid.fromString("00001812-0000-1000-8000-00805F9B34FB")

        private val KEYBOARD_DESCRIPTOR = byteArrayOf(
            0x05, 0x01, 0x09, 0x06, 0xA1.toByte(), 0x01,
            0x05, 0x07, 0x19, 0xE0.toByte(), 0x29, 0xE7.toByte(),
            0x15, 0x00, 0x25, 0x01, 0x75, 0x01, 0x95, 0x08,
            0x81.toByte(), 0x02,
            0x95, 0x01, 0x75, 0x08, 0x81.toByte(), 0x01,
            0x95, 0x05, 0x75, 0x01, 0x05, 0x08,
            0x19, 0x01, 0x29, 0x05, 0x91.toByte(), 0x02,
            0x95, 0x01, 0x75, 0x03, 0x91.toByte(), 0x01,
            0x95, 0x06, 0x75, 0x08, 0x15, 0x00, 0x25, 0x65,
            0x05, 0x07, 0x19, 0x00, 0x29, 0x65,
            0x81.toByte(), 0x00, 0xC0.toByte()
        )
    }

    fun start() {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (adapter == null) {
            statusCallback?.invoke("No Bluetooth adapter")
            return
        }

        adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile != BluetoothProfile.HID_DEVICE) return
                hidDevice = proxy as BluetoothHidDevice

                val sdp = BluetoothHidDeviceAppSdpSettings(
                    "Keyboard",
                    "AureX Bluetooth Keyboard",
                    "BuiltXStudios",
                    BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                    KEYBOARD_DESCRIPTOR
                )

                val callback = object : BluetoothHidDevice.Callback() {
                    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                        statusCallback?.invoke(
                            if (registered) "✅ Registered — pair with TV" else "❌ Registration failed"
                        )
                    }
                    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                        when (state) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                connectedDevice = device
                                statusCallback?.invoke("✅ Connected: ${device?.name}")
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                connectedDevice = null
                                statusCallback?.invoke("Disconnected")
                            }
                        }
                    }
                }

                val ok = hidDevice?.registerApp(sdp, null, null, executor, callback)
                if (ok != true) statusCallback?.invoke("❌ registerApp returned false")
            }

            override fun onServiceDisconnected(profile: Int) {
                hidDevice = null
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    fun setModifier(modifier: Byte, pressed: Boolean) {
        modifierState = if (pressed) (modifierState.toInt() or modifier.toInt()).toByte()
                       else (modifierState.toInt() and modifier.toInt().inv()).toByte()
    }

    fun sendKey(keyCode: Byte) {
        val report = byteArrayOf(modifierState, 0, keyCode, 0, 0, 0, 0, 0)
        hidDevice?.sendReport(connectedDevice, 0, report)
    }

    fun releaseKey() {
        val report = byteArrayOf(modifierState, 0, 0, 0, 0, 0, 0, 0)
        hidDevice?.sendReport(connectedDevice, 0, report)
    }
}
