package com.example.playlistmaker.util

import java.text.SimpleDateFormat
import java.util.Locale

object TimeFormatter {
    fun formatTime(millis: Long?): String {
        if (millis == null || millis <= 0) return ""

        val minutes = millis / 1000 / 60
        val seconds = millis / 1000 % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}