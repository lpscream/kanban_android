package com.delprod.kanban.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Activity.toast(@StringRes stringRes: Int){
    Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show()
}
fun Fragment.toast(@StringRes stringRes: Int, context: Context){
    Toast.makeText(this.context, stringRes, Toast.LENGTH_LONG).show()
}

fun Activity.toast(stringRes: String){
    Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show()
}
fun Fragment.toast(stringRes: String){
    Toast.makeText(this.context, stringRes, Toast.LENGTH_LONG).show()
}


fun Int.toPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()