package com.delprod.kanban.data.telegram

import com.delprod.kanban.BuildConfig
import com.delprod.kanban.core.BASE_API_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface TelegramService {

    @FormUrlEncoded
    @POST("/bot{token}/sendMessage")
    suspend fun sendMessage(
        @Path("token", encoded = true) token: String,
        @Field("chat_id") chatId: String,
        @Field("text") message: String,
        @Field("parse_mode") parseMode: String = "HTML" // или Markdown, HTML
    ): TelegramResponse



    companion object RetrofitObject{
        private fun defaultOkHTTPClient(): OkHttpClient {
            val logger = HttpLoggingInterceptor()
            // BODY logging would leak the bot token (it's in the URL path) and message text
            // into logcat in release builds.
            logger.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            val okhttpClient = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            return okhttpClient
        }


        fun create(): TelegramService {
            val gson = GsonBuilder()
                .create()

            return Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .client(defaultOkHTTPClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(TelegramService::class.java)
        }
    }
}