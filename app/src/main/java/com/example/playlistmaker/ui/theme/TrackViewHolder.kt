package com.example.playlistmaker.ui.theme

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.model.Track
import java.text.SimpleDateFormat // Добавьте этот импорт
import java.util.Locale         // Добавьте этот импорт

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val trackImage: ImageView = itemView.findViewById(R.id.track_image)
    private val trackName: TextView = itemView.findViewById(R.id.track_name)
    private val artistName: TextView = itemView.findViewById(R.id.artist_name)

    fun bind(track: Track) {
        trackName.text = track.trackName

        val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        artistName.text = "${track.artistName} • $formattedTime"

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.rounded_placeholder)
            .centerCrop()
            .into(trackImage)
    }
}