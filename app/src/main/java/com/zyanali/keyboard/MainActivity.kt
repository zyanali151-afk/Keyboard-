package com.zyanali.keyboard

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var hid: HidManager
    private lateinit var statusText: TextView

    // HID key codes (USB HID Usage Tables)
    private val keyMap = mapOf(
        // Numbers
        "1" to 0x1E.toByte(), "2" to 0x1F.toByte(), "3" to 0x20.toByte(),
        "4" to 0x21.toByte(), "5" to 0x22.toByte(), "6" to 0x23.toByte(),
        "7" to 0x24.toByte(), "8" to 0x25.toByte(), "9" to 0x26.toByte(),
        "0" to 0x27.toByte(),
        // Letters
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
        // Punctuation
        "-" to 0x2D.toByte(), "=" to 0x2E.toByte(), "[" to 0x2F.toByte(),
        "]" to 0x30.toByte(), "\\" to 0x31.toByte(), ";" to 0x33.toByte(),
        "'" to 0x34.toByte(), "`" to 0x35.toByte(), "," to 0x36.toByte(),
        "." to 0x37.toByte(), "/" to 0x38.toByte(),
        // Special
        "SPACE" to 0x2C.toByte(), "ENTER" to 0x28.toByte(),
        "BKSP" to 0x2A.toByte(), "TAB" to 0x2B.toByte(), "ESC" to 0x29.toByte(),
        "DEL" to 0x4C.toByte(), "HOME" to 0x4A.toByte(), "END" to 0x4D.toByte(),
        "PGUP" to 0x4B.toByte(), "PGDN" to 0x4E.toByte(),
        // Arrows
        "↑" to 0x52.toByte(), "↓" to 0x51.toByte(),
        "←" to 0x50.toByte(), "→" to 0x4F.toByte(),
        // F-keys
        "F1" to 0x3A.toByte(), "F2" to 0x3B.toByte(), "F3" to 0x3C.toByte(),
        "F4" to 0x3D.toByte(), "F5" to 0x3E.toByte(), "F6" to 0x3F.toByte(),
        "F7" to 0x40.toByte(), "F8" to 0x41.toByte(), "F9" to 0x42.toByte(),
        "F10" to 0x43.toByte(), "F11" to 0x44.toByte(), "F12" to 0x45.toByte()
    )

    // Modifiers
    private val SHIFT = 0x02.toByte()
    private val CTRL = 0x01.toByte()
    private val ALT = 0x04.toByte()

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
            text = "Starting HID..."
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, 0, 0, 8)
        }
        root.addView(statusText)

        hid.statusCallback = { msg -> runOnUiThread { statusText.text = msg } }

        // Row definitions
        val row1 = listOf("ESC","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12")
        val row2 = listOf("`","1","2","3","4","5","6","7","8","9","0","-","=","BKSP")
        val row3 = listOf("TAB","q","w","e","r","t","y","u","i","o","p","[","]","\\")
        val row4 = listOf("CTRL","a","s","d","f","g","h","j","k","l",";","'","ENTER")
        val row5 = listOf("SHIFT","z","x","c","v","b","n","m",",",".","/","SHIFT")
        val row6 = listOf("HOME","END","PGUP","PGDN","DEL","←","↑","↓","→")
        val row7 = listOf("SPACE")

        val allRows = listOf(row1, row2, row3, row4, row5, row6, row7)

        for (row in allRows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            }
            for (key in row) {
                val btn = Button(this).apply {
                    text = if (key.length > 4) key.substring(0, 4) else key
                    textSize = 10f
                    setPadding(0, 0, 0, 0)
                    minWidth = 0
                    minimumWidth = 0
                    setOnClickListener {
                        handleKeyPress(key)
                    }
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                        marginStart = 2; marginEnd = 2; topMargin = 2; bottomMargin = 2
                    }
                }
                rowLayout.addView(btn)
            }
            root.addView(rowLayout)
        }

        setContentView(root)
        hid.start()
    }

    private fun handleKeyPress(key: String) {
        when (key) {
            "SHIFT" -> toggleModifier(SHIFT)
            "CTRL" -> toggleModifier(CTRL)
            "ALT" -> toggleModifier(ALT)
            else -> {
                keyMap[key]?.let {
                    hid.sendKey(it)
                    hid.releaseKey()
                }
            }
        }
    }

    private var shiftOn = false
    private var ctrlOn = false
    private var altOn = false

    private fun toggleModifier(mod: Byte) {
        when (mod) {
            SHIFT -> { shiftOn = !shiftOn; hid.setModifier(SHIFT, shiftOn) }
            CTRL -> { ctrlOn = !ctrlOn; hid.setModifier(CTRL, ctrlOn) }
            ALT -> { altOn = !altOn; hid.setModifier(ALT, altOn) }
        }
    }
}
