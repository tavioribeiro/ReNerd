package com.example.renerd.core.network

import com.example.renerd.core.network.api.PodcastApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object PodcastClient {

    private const val BASE_URL = "https://api.jovemnerd.com.br/"

    private val okHttpClient = OkHttpClient.Builder()
        //.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) // Log das requisições (opcional)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: PodcastApi = retrofit.create(PodcastApi::class.java)
}
