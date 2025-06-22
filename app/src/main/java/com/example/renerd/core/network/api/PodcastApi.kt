package com.example.renerd.core.network.api


import com.example.renerd.BuildConfig
import com.example.renerd.core.network.model.EpisodeModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PodcastApi {

    @GET(BuildConfig.BASE_ROUTE_DEV)
    fun getNerdcasts(
        @Query("per_page") perPage: Int = -1,
        @Query("after") after: String,
        @Query("before") before: String
    ): Call<List<EpisodeModel>>
}