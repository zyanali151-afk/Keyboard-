package com.zyanali.keyboard

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var hid: HidManager
    private lateinit var statusText: TextView

    private val keyMap = mapOf(
        "1" to 0x1E.toByte(), "2" to 0x1F.toByte(), "3" to 0x20.toByte(),
        "4" to 0x21.toByte(), "5" to 0x22.toByte(), "6" to 0x23.toByte(),
        "7" to 0x24.toByte(), "8" to 0x25.toByte(), "9" to 0x26.toByte(),
        "0" to 0x27.toByte(),
        "q" to 0x14.toByte(), "w" to 0x1A.toByte(), "e" to 0x08.toByte(),
        "r" to 0x15.toByte(), "t" to 0x17.toByte(), "y" to 0x1C.toByte(),
        "u" to 0x18.toByte(), "i" to 0x0C.toByte(), "o" to 0x12.toByte(),
        "p" to 0x13.toByte(),
        "a" to 0x04.toByte(), "s" to 0x16.toByte(), "d" to 0x07.toByte(),
        "f" to 0x09.toByte(), "g" to 0x0A.toByte(), "h" to 0x0B.toByte(),
        "j" to 0x0D.toByte(), "k" to 0x0E.toByte(), "l" to 0x0F.toByte(),
        "z" to 0x1D.toByte(), "x" to 0x1B.toByte(), "c" to 0x06.toByte(),
        "v" to 0x19.toByte(), "b" to 0x05.toByte(), "n" to 0x11.toByte(),
        "m" to 0x10.toByte(),
        "SPACE" to 0x2C.toByte(),
        "ENTER" to 0x28.toByte(),
        "BKSP" to 0x2A.toByte()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ), 100)
        }

        hid = HidManager(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFF0F0F0F.toInt())
        }

        statusText = TextView(this).apply {
            text = "Starting..."
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, 0, 0, 8)
        }
        root.addView(statusText)

        hid.statusCallback = { msg -> runOnUiThread { statusText.text = msg } }

        val rows = listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("z","x","c","v","b","n","m"),
            listOf("SPACE","ENTER","BKSP")
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
                        keyMap[key]?.let {
                            hid.sendKey(it)
                            hid.releaseKey()
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
        hid.start()
    }
}
