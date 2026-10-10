package com.zyanali.keyboard

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Keyboard v0.1.0"
            textSize = 32f
        }

        val status = TextView(this).apply {
            text = "Bluetooth HID keyboard (in development)"
            textSize = 18f
            setPadding(0, 24, 0, 0)
        }

        layout.addView(title)
        layout.addView(status)

        setContentView(layout)
    }
}
