package com.delprod.kanban.data.auth

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * OAuth 2.0 token endpoint on the backend (RFC 6749 "password"/"refresh_token" grants).
 * [refreshBlocking] is a plain (non-suspend) [Call] because OkHttp's [okhttp3.Authenticator]
 * runs on OkHttp's own thread and must complete synchronously.
 */
interface AuthApi {

    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("oauth/token")
    fun refreshBlocking(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Call<TokenResponse>
}
