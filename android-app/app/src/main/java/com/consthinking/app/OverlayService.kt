package com.consthinking.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import java.io.File

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: TextView
    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null

    private val detector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleRecording()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                ScreenshotUploader.captureAndUpload(this@OverlayService)
                return true
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        setupOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        windowManager.removeView(overlayView)
    }

    private fun setupOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = TextView(this).apply {
            text = "思"
            alpha = 0.55f
            textSize = 24f
            setBackgroundColor(0xAA222222.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(30, 20, 30, 20)
            setOnTouchListener(DraggableTouchListener())
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            x = 0
            y = 120
        }

        windowManager.addView(overlayView, params)
    }

    private fun toggleRecording() {
        if (recorder == null) startRecording() else stopRecordingAndUpload()
    }

    private fun startRecording() {
        val output = File(cacheDir, "reflection_${System.currentTimeMillis()}.m4a")
        recordingFile = output

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }

        overlayView.text = "录"
        overlayView.alpha = 1.0f
    }

    private fun stopRecordingAndUpload() {
        stopRecording()
        overlayView.text = "思"
        overlayView.alpha = 0.55f
        recordingFile?.let { ReflectionUploader.uploadAudio(this, it) }
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            reset()
            release()
        }
        recorder = null
    }

    private fun buildNotification(): Notification {
        val channelId = "overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Overlay Service", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("Consthinking 运行中")
            .setContentText("悬浮窗已开启")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    private inner class DraggableTouchListener : View.OnTouchListener {
        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            detector.onTouchEvent(event)
            val params = v.layoutParams as WindowManager.LayoutParams

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downX).toInt()
                    val dy = (event.rawY - downY).toInt()
                    params.x = startX - dx
                    params.y = startY - dy
                    windowManager.updateViewLayout(v, params)
                }
                MotionEvent.ACTION_UP -> {
                    if (params.x < -60) {
                        overlayView.alpha = 1f
                    } else {
                        params.x = 0
                        overlayView.alpha = 0.55f
                        windowManager.updateViewLayout(v, params)
                    }
                }
            }
            return true
        }
    }
}
