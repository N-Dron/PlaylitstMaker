package com.example.playlistmaker.network

import com.example.playlistmaker.model.Track

object TrackMapper {
    fun map(apiTrack: ApiTrack): Track {
        return Track(
            trackId = apiTrack.trackId ?: 0L,
            trackName = apiTrack.trackName ?: "",
            artistName = apiTrack.artistName ?: "",
            trackTimeMillis = apiTrack.trackTimeMillis ?: 0L,
            artworkUrl100 = apiTrack.artworkUrl100 ?: "",
            collectionName = apiTrack.collectionName,
            releaseDate = apiTrack.releaseDate,
            primaryGenreName = apiTrack.primaryGenreName,
            country = apiTrack.country,
            previewUrl = apiTrack.previewUrl
        )
    }
}