package com.delprod.kanban.data.auth

import com.delprod.kanban.core.logPrint
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * On a 401, tries once to exchange the stored refresh token for a new access token and retries
 * the original request. If that fails (refresh token expired/revoked, or none stored), it clears
 * the session so the app falls back to the login screen on its next auth check, and gives up on
 * this request rather than retrying forever.
 */
class AuthAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
    private val onSessionExpired: () -> Unit
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null // already retried once, avoid infinite loop

        val refreshToken = tokenStore.refreshToken() ?: run {
            onSessionExpired()
            return null
        }

        return try {
            val newTokens = authApi.refreshBlocking(refreshToken = refreshToken).execute().body()
            if (newTokens == null) {
                tokenStore.clear()
                onSessionExpired()
                null
            } else {
                tokenStore.save(newTokens)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            }
        } catch (e: Exception) {
            logPrint("AuthAuthenticator", "refresh failed: ${e.message}")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
