package com.example.playlistmaker.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/search?entity=song")
    fun searchSongs(@Query("term") term: String): Call<SearchResponse>
}
