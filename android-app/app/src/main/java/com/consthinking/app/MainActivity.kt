package com.consthinking.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var serverInput: EditText
    private lateinit var userInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serverInput = EditText(this).apply { hint = "服务器地址，如 http://10.0.2.2:8080" }
        userInput = EditText(this).apply { hint = "用户ID" }

        val saveButton = Button(this).apply {
            text = "保存配置"
            setOnClickListener {
                AppConfig.save(
                    context = this@MainActivity,
                    baseUrl = serverInput.text.toString().trim(),
                    userId = userInput.text.toString().trim(),
                )
            }
        }

        val overlayButton = Button(this).apply {
            text = "开启悬浮窗"
            setOnClickListener {
                ensureOverlayPermission()
                startService(Intent(this@MainActivity, OverlayService::class.java))
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            addView(TextView(this@MainActivity).apply { text = "Consthinking MVP" })
            addView(serverInput)
            addView(userInput)
            addView(saveButton)
            addView(overlayButton)
        }

        setContentView(root)

        serverInput.setText(AppConfig.baseUrl(this))
        userInput.setText(AppConfig.userId(this))
    }

    private fun ensureOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            startActivity(intent)
        }
    }
}
