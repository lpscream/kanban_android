package com.delprod.kanban.google

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * Загружает Excel-файл по публичной ссылке ("Все, кто имеет ссылку"),
 * БЕЗ OAuth/access token – обычный анонимный GET-запрос.
 *
 * Поддерживает:
 * - прямая ссылка на .xls/.xlsx файл (любой сервер)
 * - ссылка на Google Sheet: https://docs.google.com/spreadsheets/d/<ID>/edit...
 * - ссылка на файл в Google Drive: https://drive.google.com/file/d/<ID>/view...
 *
 * ВАЖНО: работает только если в настройках доступа файла стоит
 * "Все, кто имеет ссылку" (Anyone with the link). Если доступ ограничен
 * конкретными аккаунтами – нужен OAuth (см. GoogleSheetsLoader).
 *
 * Ограничения: Google Drive для очень больших файлов иногда показывает промежуточную
 * страницу "невозможно проверить на вирусы" вместо самого файла - для
 * небольших xls-отчетов (как этот) это не проблема.
 */
object PublicLinkExcelLoader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val googleSheetIdRegex = Regex("""docs\.google\.com/spreadsheets/d/([a-zA-Z0-9_-]+)""")
    private val googleDriveFileIdRegex = Regex("""drive\.google\.com/file/d/([a-zA-Z0-9_-]+)""")
    private val googleDriveOpenIdRegex = Regex("""[?&]id=([a-zA-Z0-9_-]+)""")

    suspend fun download(url: String): InputStream = withContext(Dispatchers.IO) {
        val resolvedUrl = resolveDownloadUrl(url)
        val request = Request.Builder().url(resolvedUrl).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException(
                "Не вдалося завантажити файл (HTTP ${response.code}). " +
                        "Перевірте, що посилання відкрите для перегляду ('Усі, хто має посилання')."
            )
        }
        val bytes = response.body?.bytes()
            ?: throw IOException("Порожня відповідь при завантаженні файлу")
        response.close()
        bytes.inputStream()
    }

    /** Перетворює будь-яке з підтримуваних посилань на пряме посилання для завантаження байтів. */
    private fun resolveDownloadUrl(url: String): String {
        googleSheetIdRegex.find(url)?.let { m ->
            val id = m.groupValues[1]
            // format=xlsx (можна й "xls", але xlsx надійніший для сучасних таблиць)
            return "https://docs.google.com/spreadsheets/d/$id/export?format=xls"
        }
        googleDriveFileIdRegex.find(url)?.let { m ->
            val id = m.groupValues[1]
            return "https://drive.google.com/uc?export=download&id=$id"
        }
        googleDriveOpenIdRegex.find(url)?.let { m ->
            val id = m.groupValues[1]
            return "https://drive.google.com/uc?export=download&id=$id"
        }
        // Пряме посилання на файл — використовуємо як є
        return url
    }
}
