package com.zyanali.keyboard

import android.bluetooth.*
import android.content.Context
import android.os.ParcelUuid

class HidDeviceManager(private val context: Context) {

    companion object {
        // HID Service UUID
        val HID_SERVICE_UUID: ParcelUuid =
            ParcelUuid.fromString("00001812-0000-1000-8000-00805F9B34FB")

        // HID Report descriptor for a simple keyboard
        val KEYBOARD_REPORT_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(),        // Usage Page (Generic Desktop)
            0x09.toByte(), 0x06.toByte(),        // Usage (Keyboard)
            0xA1.toByte(), 0x01.toByte(),        // Collection (Application)
            0x05.toByte(), 0x07.toByte(),        //   Usage Page (Key Codes)
            0x19.toByte(), 0xE0.toByte(),        //   Usage Minimum (224)
            0x29.toByte(), 0xE7.toByte(),        //   Usage Maximum (231)
            0x15.toByte(), 0x00.toByte(),        //   Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(),        //   Logical Maximum (1)
            0x75.toByte(), 0x01.toByte(),        //   Report Size (1)
            0x95.toByte(), 0x08.toByte(),        //   Report Count (8)
            0x81.toByte(), 0x02.toByte(),        //   Input (Data, Variable, Absolute)
            0x95.toByte(), 0x01.toByte(),        //   Report Count (1)
            0x75.toByte(), 0x08.toByte(),        //   Report Size (8)
            0x81.toByte(), 0x01.toByte(),        //   Input (Constant) reserved byte
            0x95.toByte(), 0x05.toByte(),        //   Report Count (5)
            0x75.toByte(), 0x01.toByte(),        //   Report Size (1)
            0x05.toByte(), 0x08.toByte(),        //   Usage Page (LEDs)
            0x19.toByte(), 0x01.toByte(),        //   Usage Minimum (1)
            0x29.toByte(), 0x05.toByte(),        //   Usage Maximum (5)
            0x91.toByte(), 0x02.toByte(),        //   Output (Data, Variable, Absolute)
            0x95.toByte(), 0x01.toByte(),        //   Report Count (1)
            0x75.toByte(), 0x03.toByte(),        //   Report Size (3)
            0x91.toByte(), 0x01.toByte(),        //   Output (Constant) padding
            0x95.toByte(), 0x06.toByte(),        //   Report Count (6)
            0x75.toByte(), 0x08.toByte(),        //   Report Size (8)
            0x15.toByte(), 0x00.toByte(),        //   Logical Minimum (0)
            0x25.toByte(), 0x65.toByte(),        //   Logical Maximum (101)
            0x05.toByte(), 0x07.toByte(),        //   Usage Page (Key Codes)
            0x19.toByte(), 0x00.toByte(),        //   Usage Minimum (0)
            0x29.toByte(), 0x65.toByte(),        //   Usage Maximum (101)
            0x81.toByte(), 0x00.toByte(),        //   Input (Data, Array)
            0xC0.toByte()                        // End Collection
        )
    }

    private var hidDevice: BluetoothHidDevice? = null
    private var registeredDevice: BluetoothDevice? = null

    fun register(callback: (Boolean, String) -> Unit) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: return callback(false, "No Bluetooth")

        hidDevice = adapter.getProfileProxy(
            context,
            object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == BluetoothProfile.HID_DEVICE) {
                        hidDevice = proxy as BluetoothHidDevice
                        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
                            "Keyboard",
                            "AureX Bluetooth Keyboard",
                            "BuiltXStudios",
                            BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                            KEYBOARD_REPORT_DESCRIPTOR
                        )
                        val ok = hidDevice?.registerApp(
                            sdpSettings, null, null,
                            { /* executor */ null },
                            object : BluetoothHidDevice.Callback() {
                                override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                                    callback(registered, if (registered) "Registered" else "Not registered")
                                }
                                override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                                    when (state) {
                                        BluetoothProfile.STATE_CONNECTED -> {
                                            registeredDevice = device
                                            callback(true, "Connected to ${device?.name ?: "device"}")
                                        }
                                        BluetoothProfile.STATE_DISCONNECTED -> {
                                            registeredDevice = null
                                            callback(false, "Disconnected")
                                        }
                                    }
                                }
                            }
                        ) ?: false
                        if (!ok) callback(false, "registerApp failed")
                    }
                }
                override fun onServiceDisconnected(profile: Int) {}
            },
            BluetoothProfile.HID_DEVICE
        )
    }

    fun sendKey(modifier: Byte, keyCode: Byte) {
        val report = byteArrayOf(modifier, 0x00, keyCode, 0, 0, 0, 0, 0)
        hidDevice?.sendReport(registeredDevice, 0, report)
    }

    fun releaseKeys() {
        val report = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        hidDevice?.sendReport(registeredDevice, 0, report)
    }
}
