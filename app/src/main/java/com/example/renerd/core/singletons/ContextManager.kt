package com.example.renerd.core.singletons

import android.content.Context


object ContextManager {
    lateinit var appContext: Context

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }


    fun getContext(): Context {
        return appContext
    }
}