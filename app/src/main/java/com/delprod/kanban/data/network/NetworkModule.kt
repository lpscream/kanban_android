package com.delprod.kanban.data.network

import com.delprod.kanban.BuildConfig
import com.delprod.kanban.data.api.KanbanApiService
import com.delprod.kanban.data.auth.AuthApi
import com.delprod.kanban.data.auth.AuthAuthenticator
import com.delprod.kanban.data.auth.AuthInterceptor
import com.delprod.kanban.data.auth.TokenStore
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the Retrofit clients for the backend (see /backend). Kept separate from
 * [com.delprod.kanban.data.telegram.TelegramService], which talks to a different host.
 */
object NetworkModule {

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        // BODY logging would leak bearer tokens and business data into logcat in release builds.
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    fun createAuthApi(baseUrl: String, gson: Gson): AuthApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()
        return retrofitFor(baseUrl, client, gson).create(AuthApi::class.java)
    }

    fun createKanbanApi(
        baseUrl: String,
        gson: Gson,
        tokenStore: TokenStore,
        authApi: AuthApi,
        onSessionExpired: () -> Unit
    ): KanbanApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(loggingInterceptor())
            .authenticator(AuthAuthenticator(tokenStore, authApi, onSessionExpired))
            .build()
        return retrofitFor(baseUrl, client, gson).create(KanbanApiService::class.java)
    }

    private fun retrofitFor(baseUrl: String, client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}
