package com.zyanali.keyboard

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var hidManager: HidDeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hidManager = HidDeviceManager(this)

        // Request Bluetooth permissions
        requestBluetoothPermissions()

        // Build UI
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFF1A1A1A.toInt())
        }

        // Status bar
        statusText = TextView(this).apply {
            text = "Initializing..."
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, 0, 0, 16)
        }
        root.addView(statusText)

        // Keyboard rows
        val rows = listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("z","x","c","v","b","n","m"),
            listOf("SPACE","ENTER","BKSP")
        )

        val keyMap = mapOf(
            "a" to 0x04.toByte(), "b" to 0x05.toByte(), "c" to 0x06.toByte(),
            "d" to 0x07.toByte(), "e" to 0x08.toByte(), "f" to 0x09.toByte(),
            "g" to 0x0A.toByte(), "h" to 0x0B.toByte(), "i" to 0x0C.toByte(),
            "j" to 0x0D.toByte(), "k" to 0x0E.toByte(), "l" to 0x0F.toByte(),
            "m" to 0x10.toByte(), "n" to 0x11.toByte(), "o" to 0x12.toByte(),
            "p" to 0x13.toByte(), "q" to 0x14.toByte(), "r" to 0x15.toByte(),
            "s" to 0x16.toByte(), "t" to 0x17.toByte(), "u" to 0x18.toByte(),
            "v" to 0x19.toByte(), "w" to 0x1A.toByte(), "x" to 0x1B.toByte(),
            "y" to 0x1C.toByte(), "z" to 0x1D.toByte(),
            "1" to 0x1E.toByte(), "2" to 0x1F.toByte(), "3" to 0x20.toByte(),
            "4" to 0x21.toByte(), "5" to 0x22.toByte(), "6" to 0x23.toByte(),
            "7" to 0x24.toByte(), "8" to 0x25.toByte(), "9" to 0x26.toByte(),
            "0" to 0x27.toByte(),
            "SPACE" to 0x2C.toByte(),
            "ENTER" to 0x28.toByte(),
            "BKSP" to 0x2A.toByte()
        )

        for (row in rows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            }
            for (key in row) {
                val btn = Button(this).apply {
                    text = key
                    textSize = 14f
                    setOnClickListener {
                        val code = keyMap[key]
                        if (code != null) {
                            hidManager.sendKey(0, code)
                            hidManager.releaseKeys()
                        }
                    }
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                        marginStart = 4; marginEnd = 4; topMargin = 4; bottomMargin = 4
                    }
                }
                rowLayout.addView(btn)
            }
            root.addView(rowLayout)
        }

        setContentView(root)

        // Register HID
        hidManager.register { success, msg ->
            runOnUiThread { statusText.text = msg }
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val perms = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            ActivityCompat.requestPermissions(this, perms, 100)
        }
    }
}
