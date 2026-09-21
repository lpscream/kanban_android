package com.delprod.kanban.data

import android.content.Context
import com.delprod.kanban.data.goods.BarcodeData
import com.delprod.kanban.data.goods.ProductModel
import com.delprod.kanban.data.goods.Order
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class Settings(val context: Context, val gson: Gson) {
    private val settings = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val editor = settings.edit()

    fun botId(botId: String) {
        editor.putString("bot_id", botId)
        editor.apply()
    }

    fun chatId(chatId: String) {
        editor.putString("chat_id", chatId)
        editor.apply()
    }

    // No hardcoded default here on purpose: a real bot token must never ship in source/APK.
    // Configure it from the Settings screen instead.
    fun botId() = settings.getString("bot_id", "")

    fun chatId() = settings.getString("chat_id", "")
    fun goodsItemNomenclature(item: ProductModel) {
        val items = goodsItemNomenclature()
        if (items != null && items.size > 0) {
            if (!items.any { item.article == it.article }) {
                items.add(item)
                itemsInStorage(items)
            }
        } else {
            items?.add(item)
            itemsInStorage(items!!)
        }
    }

    fun goodsItemNomenclature(): MutableList<ProductModel>? {
        val json = settings.getString("NOMECLATURE", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<ProductModel>?>() {}.type
            gson.fromJson(json, type)
        } else mutableListOf()
    }

    fun export() = settings.getString("NOMECLATURE", null)

    fun import(import: String) {
        settings.edit().putString("NOMECLATURE", import).apply()
    }


    fun goodsItemNomenclature(article: Int): ProductModel? {
        val items = goodsItemNomenclature()
        val item: ProductModel? = items?.find { it.article == article }
        return item
    }

    fun updateProduct(item: ProductModel): MutableList<ProductModel>? {
        val items = goodsItemNomenclature()
        if (items != null) {
            val index = items.indexOfFirst { it.article == item.article }
            if (index != -1) {
                items[index] = items[index].copy(
                    name = item.name
                )
                itemsInStorage(items)
            }
        }
        return goodsItemNomenclature()
    }

    private fun itemsInStorage(items: MutableList<ProductModel>) {
        items.groupBy { it.article }
        val json = gson.toJson(items)
        settings.edit().putString("NOMECLATURE", json).apply()
    }

    fun order(): Order? {
        val json = settings.getString("ORDERS", null)
        return if (json != null) {
            val type = object : TypeToken<Order?>() {}.type
            gson.fromJson(json, type)
        } else null
    }

    fun order(orders: Order?) {
        val json = gson.toJson(orders)
        settings.edit().putString("ORDERS", json).apply()
    }

    fun customerOrder(customerName: String): Order? {
        val items: MutableList<Order>? = orders()
        return if (items != null) {
            val index = items.indexOfFirst { it.client == customerName }
            return if (index != -1) {
                items[index]
            } else null
        } else null
    }
    fun putCustomerOrder(customerName: String){
        val items: MutableList<Order>? = orders()
        if (items != null){

        }else{
            
        }
    }

    fun customerOrder(order: Order) {
        val items: MutableList<Order>? = orders()
        if(items != null){
            val index = items.indexOfFirst { it.client == order.client }
            if (index != -1){
                items[index] = items[index].copy(
                    sumBoxQuantity = order.sumBoxQuantity,
                    sumWeight = order.sumWeight,
                    comment = order.comment,
                    client = order.client,
                    date = order.date,
                    goods = order.goods
                )
                orders(items)
            }
        }
    }

    fun clients(): List<String>?{
        val orders = orders()
        val clintsList = listOf<String>()
        return if (orders != null){
            orders.forEach {
                clintsList.plus(it.client)
            }
            return clintsList
        }else null
    }

    private fun orders(orders: MutableList<Order>){
        orders.groupBy { it.client }
        val json = gson.toJson(orders)
        settings.edit().putString("CUSTOMERS_ORDERS", json).apply()
    }

    private fun orders(): MutableList<Order>? {
        val json = settings.getString("CUSTOMERS_ORDERS", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Order>?>() {}.type
            gson.fromJson(json, type)
        } else null
    }



    fun savedState(): BarcodeData? {
        val json = settings.getString("ADD_ITEM_MANUAL_SAVED_STATE", null)
        return if (json != null) {
            val type = object : TypeToken<BarcodeData?>() {}.type
            gson.fromJson(json, type)
        } else null
    }

    fun savedState(cache: BarcodeData?) {
        val json = gson.toJson(cache)
        settings.edit().putString("ADD_ITEM_MANUAL_SAVED_STATE", json).apply()
    }

    fun googleSheetUrl(url: String){
        settings.edit().putString("GOOGLE_SHEET_URL", url).apply()
    }

    fun googleSheetUrl() = settings.getString("GOOGLE_SHEET_URL", "")

    fun serverBaseUrl(url: String) {
        settings.edit().putString("SERVER_BASE_URL", url).apply()
    }

    // 10.0.2.2 is the Android emulator's alias for the host machine's localhost, so a locally
    // run backend (docker compose up in /backend) is reachable out of the box while developing.
    fun serverBaseUrl() = settings.getString("SERVER_BASE_URL", "http://10.0.2.2:8080/") ?: "http://10.0.2.2:8080/"



    fun password(pass: String) {
        val json = gson.toJson(pass)
        settings.edit().putString("PASSWORD", json).apply()
    }

    fun password(): String? {
        val json = settings.getString("PASSWORD", "8020")
        return if (json != null) {
            val type = object : TypeToken<String?>() {}.type
            gson.fromJson(json, type)
        } else null
    }


    fun lastDate(pass: String) {
        val json = gson.toJson(pass)
        settings.edit().putString("LAST_DATE", json).apply()
    }

    fun lastDate(): String? {
        val json = settings.getString("LAST_DATE", null)
        return if (json != null) {
            val type = object : TypeToken<String?>() {}.type
            gson.fromJson(json, type)
        } else null
    }

    fun lastWeight(pass: String) {
        val json = gson.toJson(pass)
        settings.edit().putString("LAST_WEIGHT", json).apply()
    }

    fun lastWeight(): String? {
        val json = settings.getString("LAST_WEIGHT", null)
        return if (json != null) {
            val type = object : TypeToken<String?>() {}.type
            gson.fromJson(json, type)
        } else null
    }

    fun bottomCurrentStateVisibility(): Boolean
         = settings.getBoolean("BOTTOM_CURRENT_STATE_VISIBILITY", true)

    fun bottomCurrentStateVisibility(state: Boolean){
        settings.edit { putBoolean("BOTTOM_CURRENT_STATE_VISIBILITY", state) }
    }



}



