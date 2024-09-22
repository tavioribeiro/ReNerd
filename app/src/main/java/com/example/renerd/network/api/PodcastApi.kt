package com.example.renerd.network.api


import com.example.renerd.network.model.PodcastModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PodcastApi {

    @GET("wp-json/jovemnerd/v1/nerdcasts/")
    fun getNerdcasts(
        @Query("per_page") perPage: Int = -1,
        @Query("after") after: String,
        @Query("before") before: String
    ): Call<List<PodcastModel>>
}