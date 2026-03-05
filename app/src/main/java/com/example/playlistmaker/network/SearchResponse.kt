package com.example.playlistmaker.network

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("resultCount")
    val resultCount: Int = 0,

    @SerializedName("results")
    val results: List<ApiTrack> = emptyList()
)

data class ApiTrack(
    @SerializedName("trackId")
    val trackId: Long? = null,

    @SerializedName("trackName")
    val trackName: String? = null,

    @SerializedName("artistName")
    val artistName: String? = null,

    @SerializedName("trackTimeMillis")
    val trackTimeMillis: Long? = null,

    @SerializedName("artworkUrl100")
    val artworkUrl100: String? = null,

    @SerializedName("collectionName")
    val collectionName: String? = null,

    @SerializedName("releaseDate")
    val releaseDate: String? = null,

    @SerializedName("primaryGenreName")
    val primaryGenreName: String? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("previewUrl")
    val previewUrl: String? = null
)