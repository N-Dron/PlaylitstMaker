package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(context: Context) {

    private val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HISTORY = "history_list"
        private const val MAX_SIZE = 10
    }

    fun saveTrack(track: Track) {
        val list = getHistory().toMutableList()

        list.removeAll { it.trackName == track.trackName }

        list.add(0, track)

        if (list.size > MAX_SIZE) {
            list.removeAt(list.lastIndex)
        }

        val json = gson.toJson(list)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun getHistory(): List<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()

        val type = object : TypeToken<List<Track>>() {}.type

        return gson.fromJson(json, type) ?: emptyList()
    }

    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
