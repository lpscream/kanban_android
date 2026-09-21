package com.delprod.kanban.data.auth

import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val gson: Gson = Gson()
) {

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val tokens = authApi.login(username = username, password = password)
        tokenStore.save(tokens)
    }.recoverCatching { error ->
        throw RuntimeException(errorMessageOf(error), error)
    }

    private fun errorMessageOf(error: Throwable): String {
        if (error !is HttpException) return error.message ?: "Не удалось подключиться к серверу"
        val body = error.response()?.errorBody()?.string()
        val parsed = body?.let { runCatching { gson.fromJson(it, BackendError::class.java) }.getOrNull() }
        return parsed?.message ?: "Неверный логин или пароль"
    }

    private data class BackendError(val error: String?, val message: String?)

    fun logout() = tokenStore.clear()

    fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    fun isAdmin(): Boolean = tokenStore.isAdmin()
}
