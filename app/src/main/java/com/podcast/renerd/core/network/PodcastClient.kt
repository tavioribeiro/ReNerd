package com.podcast.renerd.core.network

import com.podcast.renerd.BuildConfig
import com.podcast.renerd.core.network.api.PodcastApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PodcastClient {

    val api: PodcastApi

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_DEV)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(PodcastApi::class.java)
    }
}
