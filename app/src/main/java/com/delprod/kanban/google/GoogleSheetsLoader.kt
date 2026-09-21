package com.delprod.kanban.google

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream

/**
 * Загружает Google Sheet как .xls через Drive API "export" endpoint,
 * чтобы в дальнейшем использовать тот же FoodOrderXlsParser.
 *
 * Нуждается:
 * - авторизацию Google (Credential Manager / GoogleSignIn) со scope
 * "https://www.googleapis.com/auth/drive.readonly" или "drive.file"
 * - fileId — id Google Sheet (с URL: /d/<fileId>/edit)
 *
 * Пример получения accessToken через com.google.android.gms.auth.api.identity
 * или новый Credential Manager API – вынесен за пределы этого класса,
 * потому что это зависит от того, как именно в проекте реализован sign-in.
 */
class GoogleSheetsLoader(
    private val accessTokenProvider: suspend () -> String,
    private val client: OkHttpClient = OkHttpClient()
) {

    suspend fun downloadAsXls(fileId: String): InputStream = withContext(Dispatchers.IO) {
        val token = accessTokenProvider()
        val url = "https://www.googleapis.com/drive/v3/files/$fileId/export" +
            "?mimeType=application/vnd.ms-excel"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException("Не удалось загрузить Google Sheet: HTTP ${response.code}")
        }
        // Копіюємо в byte buffer, щоб можна було закрити response і безпечно віддати stream далі
        val bytes = response.body?.bytes() ?: throw IOException("Пустой ответ от Google Drive")
        response.close()
        bytes.inputStream()
    }

    /** Витягує fileId з посилання виду https://docs.google.com/spreadsheets/d/<fileId>/edit#... */
    companion object {
        fun extractFileId(shareUrl: String): String? =
            Regex("/d/([a-zA-Z0-9_-]+)").find(shareUrl)?.groupValues?.get(1)
    }
}
