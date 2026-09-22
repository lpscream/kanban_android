package com.delprod.kanban.UI.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.R
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.core.toUserMessage
import com.delprod.kanban.data.Settings
import com.delprod.kanban.data.telegram.TelegramRepository
import com.delprod.kanban.data.telegram.TelegramResponse
import com.delprod.kanban.db.ExportData
import com.delprod.kanban.repository.ImportRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ImportRepository,
    private val settings: Settings,
    private val telegramRepository: TelegramRepository,
    private val gson: Gson): ViewModel() {

    private val TAG = "SettingsViewModel_log"

    private val loadingMutableStateFlow = MutableStateFlow(false)
    private val chatIdStateFlow = MutableStateFlow("")
    private val botIdStateFlow = MutableStateFlow("")
    private val testConnectionStateFlow = MutableStateFlow<TelegramResponse?>(null)
    private val sendOrderStateFlow = MutableStateFlow<TelegramResponse?>(null)
    private val toastEventChannel = Channel<Int>(Channel.BUFFERED)




    val loadingFlow: Flow<Boolean>
        get() = loadingMutableStateFlow.asStateFlow()

    val chatIdFlow: Flow<String>
        get() = chatIdStateFlow.asStateFlow()

    val botIdFlow: Flow<String>
        get() = botIdStateFlow.asStateFlow()

    val testConnectionFlow: Flow<TelegramResponse?>
        get() = testConnectionStateFlow.asStateFlow()

    val sendOrderFlow: Flow<TelegramResponse?>
        get() = sendOrderStateFlow.asStateFlow()

    val toastFlow: Flow<Int>
        get() = toastEventChannel.receiveAsFlow()



    fun fetchChatId(){
        viewModelScope.launch {
            runCatching {
                settings.chatId()
            }.onSuccess {
                chatIdStateFlow.value = it.toString()
            }.onFailure {
                toastEventChannel.trySendBlocking(R.string.error_fetching_chat_id)
            }
        }
    }


    fun fetchBotId(){
        viewModelScope.launch {
            runCatching {
                settings.botId()
            }.onSuccess {
                botIdStateFlow.value = it.toString()
            }.onFailure {
                toastEventChannel.trySendBlocking(R.string.error_fetching_bot_id)
            }
        }
    }

    fun chatId() = settings.chatId()

    fun botId() = settings.botId()


    fun chatId(chatId: String){
        settings.chatId(chatId)
    }

    fun botId(botId: String){
        settings.botId(botId)
    }


    fun testConnection(){
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                telegramRepository.sendMessage(
                    settings.botId().toString(),
                    settings.chatId().toString(),
                    "Hello world!!!")
            }.onSuccess {
                Log.d(TAG, "testConnection: $it")
                testConnectionStateFlow.value = it
                loadingMutableStateFlow.value = false
            }.onFailure {
                Log.d(TAG, "testConnection: error $it")
                toastEventChannel.trySendBlocking(R.string.error_test_connection)
                loadingMutableStateFlow.value = false
            }
        }
    }

    fun sendOrder(orderMessage: String){
        loadingMutableStateFlow.value = true
        viewModelScope.launch {
            runCatching {
                logPrint("sendOrder(message: $orderMessage)")
                telegramRepository.sendMessage(
                    settings.botId().toString(),
                    settings.chatId().toString(),
                    orderMessage)
            }.onSuccess {
                sendOrderStateFlow.value = it
                loadingMutableStateFlow.value = false
            }.onFailure {
                sendOrderStateFlow.value = null
                loadingMutableStateFlow.value = false
            }
        }
    }

    fun exportSettings(onSuccess: (String) -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.exportDataBase()
            }.onSuccess {
                onSuccess(it)
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    fun importSettings(
        import: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.importAll(parseExport(import))
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    private fun parseExport(json: String): ExportData?{
        return if (json != null){
            logPrint("json: ${json}")
            val type = object : TypeToken<ExportData>() {}.type
            gson.fromJson(json, type)
        }else null
    }


}