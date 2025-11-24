package com.podcast.renerd.core.utils

import android.util.Log
import com.podcast.renerd.BuildConfig

fun log(valor: Any?, tag: String? = null, cor: String? = null) {
    if (BuildConfig.DEBUG) {
        val tagToUse = tag ?: "MinhaTag"

        when (cor) {
            "e" -> Log.e(tagToUse, valor?.toString() ?: "null")
            "d" -> Log.d(tagToUse, valor?.toString() ?: "null")
            "i", null -> Log.i(tagToUse, valor?.toString() ?: "null")
            else -> Log.i(tagToUse, valor?.toString() ?: "null")
        }
    }
}