package com.example.renerd.core.extentions

import android.content.Context

object ContextManager {
    lateinit var appContext: Context

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    fun getGlobalContext(): Context {
        return appContext
    }
}