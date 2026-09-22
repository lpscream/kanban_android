package com.delprod.kanban.core

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Turns a network/repository failure into a message safe to show the user (Toast or error
 * TextView) instead of a raw exception dump — host names, stack frames, "java.net.*" class
 * names — leaking into the UI.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "Нет соединения с сервером. Проверьте подключение к интернету."
    is SocketTimeoutException -> "Сервер не отвечает. Попробуйте ещё раз позже."
    is HttpException -> when (code()) {
        401 -> "Сессия истекла. Войдите в приложение заново."
        403 -> "Недостаточно прав для выполнения этого действия."
        404 -> "Запрошенные данные не найдены на сервере."
        in 500..599 -> "Ошибка на сервере. Попробуйте позже."
        else -> "Сервер вернул ошибку (код ${code()})."
    }
    is IOException -> "Ошибка сети. Проверьте подключение к интернету."
    else -> message?.takeIf { it.isNotBlank() } ?: "Неизвестная ошибка. Попробуйте ещё раз."
}
