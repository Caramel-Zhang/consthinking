package com.consthinking.app

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.Instant

object ReflectionUploader {
    private val client = OkHttpClient()

    fun uploadAudio(context: Context, file: File) {
        val baseUrl = AppConfig.baseUrl(context)
        val userId = AppConfig.userId(context)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("ts", Instant.now().toString())
            .addFormDataPart(
                "audio",
                file.name,
                file.asRequestBody("audio/mp4".toMediaType()),
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/reflection/audio")
            .post(requestBody)
            .build()

        Thread {
            client.newCall(request).execute().close()
            file.delete()
        }.start()
    }
}
