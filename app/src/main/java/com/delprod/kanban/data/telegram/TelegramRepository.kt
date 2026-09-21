package com.delprod.kanban.data.telegram


class TelegramRepository(val service: TelegramService) {

    suspend fun sendMessage(token: String, chatId: String, message: String) =
        service.sendMessage(token, chatId, message)
}