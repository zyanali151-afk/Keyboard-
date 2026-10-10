package com.zyanali.keyboard

import android.bluetooth.*
import android.content.Context

class HidManager(private val context: Context) {

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    var statusCallback: ((String) -> Unit)? = null

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

    fun start() {
        try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = btManager.adapter ?: run {
                statusCallback?.invoke("No Bluetooth")
                return
            }

            adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile != BluetoothProfile.HID_DEVICE) return
                    hidDevice = proxy as BluetoothHidDevice

                    val sdp = BluetoothHidDeviceAppSdpSettings(
                        "Keyboard",
                        "AureX Keyboard",
                        "BuiltXStudios",
                        BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                        KEYBOARD_DESCRIPTOR
                    )

                    val cb = object : BluetoothHidDevice.Callback() {
                        override fun onAppStatusChanged(device: BluetoothDevice?, registered: Boolean) {
                            statusCallback?.invoke(if (registered) "Registered" else "Not registered")
                        }
                        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                connectedDevice = device
                                statusCallback?.invoke("Connected")
                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                                connectedDevice = null
                                statusCallback?.invoke("Disconnected")
                            }
                        }
                    }

                    hidDevice?.registerApp(sdp, null, null, null, cb)
                }

                override fun onServiceDisconnected(profile: Int) {
                    hidDevice = null
                }
            }, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            statusCallback?.invoke("Error: ${e.message}")
        }
    }

    fun sendKey(keyCode: Byte) {
        try {
            val report = byteArrayOf(0, 0, keyCode, 0, 0, 0, 0, 0)
            hidDevice?.sendReport(connectedDevice, 0, report)
        } catch (e: Exception) {
            statusCallback?.invoke("Send error: ${e.message}")
        }
    }

    fun releaseKey() {
        try {
            val report = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
            hidDevice?.sendReport(connectedDevice, 0, report)
        } catch (e: Exception) {
            statusCallback?.invoke("Release error: ${e.message}")
        }
    }
}
