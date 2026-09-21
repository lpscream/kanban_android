package com.delprod.kanban.core

import android.util.Log
import com.delprod.kanban.BuildConfig


fun logPrint(message: String){
        if (BuildConfig.DEBUG) {
            Log.d("MeteoraRetail", "logPrint: $message")
        }
    }

fun logPrint(tag: String, message: String){
    if (BuildConfig.DEBUG) {
        Log.d(tag, "logPrint: $message")
    }
}
