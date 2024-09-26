package com.example.renerd.app

import android.app.Application
import android.content.Context
import com.example.renerd.core.extentions.ContextManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.binds
import org.koin.dsl.module

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ContextManager.init(this)

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(MainModule.instance)
        }
    }
}