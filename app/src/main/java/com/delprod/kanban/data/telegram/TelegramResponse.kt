package com.delprod.kanban.data.telegram

import com.google.gson.annotations.SerializedName

data class TelegramResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("result")
    val result: ResponseResult?,
    @SerializedName("error_code")
    val errorCode: String?,
    @SerializedName("description")
    val description: String?
)


data class ResponseResult(
    @SerializedName("message_id")
    val message_id: String,
    @SerializedName("from")
    val from: Responder,
    @SerializedName("chat")
    val chat: Chat,
    @SerializedName("date")
    val date: Long,
    @SerializedName("text")
    val text: String
)


data class Responder(
    @SerializedName("id")
    val id: String,
    @SerializedName("is_bot")
    val is_bot: Boolean,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("username")
    val username: String,
)

data class Chat(
    @SerializedName("id")
    val id: String,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("type")
    val type: String
)
