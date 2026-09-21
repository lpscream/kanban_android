package com.delprod.kanban.core

import android.app.Application
import android.content.Context
import android.content.Intent
import com.delprod.kanban.UI.auth.LoginActivity
import com.delprod.kanban.UI.clients.ClientsViewModel
import com.delprod.kanban.UI.goods.NomenclatureViewModel
import com.delprod.kanban.UI.label.LabelViewModel
import com.delprod.kanban.UI.orders.DatabaseViewModel
import com.delprod.kanban.UI.placing.PlaicingOrderViewModel
import com.delprod.kanban.UI.settings.SettingsViewModel
import com.delprod.kanban.data.Settings
import com.delprod.kanban.data.api.KanbanApiService
import com.delprod.kanban.data.auth.AuthApi
import com.delprod.kanban.data.auth.AuthRepository
import com.delprod.kanban.data.auth.TokenStore
import com.delprod.kanban.data.network.NetworkModule
import com.delprod.kanban.data.telegram.TelegramRepository
import com.delprod.kanban.data.telegram.TelegramService
import com.delprod.kanban.repository.ImportRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class App: Application() {

    lateinit var retrofit: TelegramService
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var settings: Settings
    lateinit var telegramRepository: TelegramRepository
    lateinit var databaseViewModel: DatabaseViewModel
    lateinit var nomenclatureListViewModel: NomenclatureViewModel
    lateinit var importRepository: ImportRepository
    lateinit var labelViewModel: LabelViewModel

    lateinit var tokenStore: TokenStore
    lateinit var authApi: AuthApi
    lateinit var kanbanApi: KanbanApiService
    lateinit var authRepository: AuthRepository

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    lateinit var placingOrderViewModel: PlaicingOrderViewModel
    lateinit var clientsViewModel: ClientsViewModel

    override fun onCreate() {
        super.onCreate()
        settings = Settings(applicationContext, gson)
        tokenStore = TokenStore(applicationContext)

        val baseUrl = settings.serverBaseUrl()
        authApi = NetworkModule.createAuthApi(baseUrl, gson)
        kanbanApi = NetworkModule.createKanbanApi(
            baseUrl = baseUrl,
            gson = gson,
            tokenStore = tokenStore,
            authApi = authApi,
            onSessionExpired = ::navigateToLogin
        )
        authRepository = AuthRepository(authApi, tokenStore, gson)

        importRepository = ImportRepository(kanbanApi, gson)
        retrofit = TelegramService.RetrofitObject.create()
        telegramRepository = TelegramRepository(retrofit)
        settingsViewModel = SettingsViewModel(importRepository,settings, telegramRepository, gson)
        databaseViewModel = DatabaseViewModel(importRepository, settings)
        nomenclatureListViewModel = NomenclatureViewModel(importRepository, settings)
        labelViewModel = LabelViewModel(importRepository)
        clientsViewModel = ClientsViewModel(importRepository)
        placingOrderViewModel = PlaicingOrderViewModel(importRepository)
    }

    /** Called from [com.delprod.kanban.data.auth.AuthAuthenticator] when the refresh token is gone/expired. */
    private fun navigateToLogin() {
        tokenStore.clear()
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: App
        val context: Context
            get() {
                return instance.applicationContext
            }
    }
}
