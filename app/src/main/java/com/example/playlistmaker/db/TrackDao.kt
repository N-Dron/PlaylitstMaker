package com.example.playlistmaker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)


    @Delete
    suspend fun deleteTrack(track: TrackEntity)


    @Query("SELECT * FROM favorite_tracks_table ORDER BY addedAt DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>


    @Query("SELECT trackId FROM favorite_tracks_table")
    fun getFavoriteTrackIds(): Flow<List<Long>>
}