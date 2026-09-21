package com.delprod.kanban.data.auth

import com.google.gson.annotations.SerializedName

/** Mirrors the backend's TokenResponse (see backend/.../models/AuthDtos.kt). */
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("role") val role: String
)

data class UserDto(
    val id: Long,
    val username: String,
    val role: String
)

data class CreateUserRequest(
    val username: String,
    val password: String,
    val role: String = "USER"
)
