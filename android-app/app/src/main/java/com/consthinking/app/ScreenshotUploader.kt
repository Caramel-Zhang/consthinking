package com.consthinking.app

import android.content.Context
import java.io.File

object ScreenshotUploader {
    fun captureAndUpload(context: Context) {
        // TODO:
        // 1) 使用 MediaProjection 走一次用户授权流程
        // 2) 捕获屏幕位图写入 cacheDir 临时文件（不要进入系统相册）
        // 3) 通过 multipart 上传到 /reflection/screenshot
        // 4) 上传成功后删除临时文件
    }

    fun uploadCachedScreenshot(context: Context, file: File) {
        // 预留：和 ReflectionUploader 类似，用 multipart 上传 screenshot 字段
        file.delete()
    }
}
