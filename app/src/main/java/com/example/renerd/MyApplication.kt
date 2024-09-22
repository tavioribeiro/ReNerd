package com.example.renerd

import android.app.Application
import android.content.Context
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.binds
import org.koin.dsl.module

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            module {
                single { this@MyApplication } binds arrayOf(Context::class, Application::class)
            }
            modules(MainModule.instance)
        }
    }
}