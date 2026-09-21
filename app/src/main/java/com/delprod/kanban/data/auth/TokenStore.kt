package com.delprod.kanban.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Access/refresh tokens are credentials, unlike the rest of [com.delprod.kanban.data.Settings]'s
 * plain SharedPreferences — they're kept in their own encrypted-at-rest file.
 */
class TokenStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "auth_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun save(tokens: TokenResponse) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putString(KEY_ROLE, tokens.role)
            .apply()
    }

    fun accessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun refreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun role(): String? = prefs.getString(KEY_ROLE, null)
    fun isAdmin(): Boolean = role() == "ADMIN"

    fun isLoggedIn(): Boolean = accessToken() != null

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ROLE = "role"
    }
}
