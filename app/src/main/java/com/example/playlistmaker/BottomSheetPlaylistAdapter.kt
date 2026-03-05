package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.db.PlaylistEntity
import java.io.File

class BottomSheetPlaylistAdapter(
    private var playlists: List<PlaylistEntity>,
    private val onPlaylistClick: (PlaylistEntity) -> Unit
) : RecyclerView.Adapter<BottomSheetPlaylistAdapter.PlaylistViewHolder>() {

    fun updatePlaylists(newPlaylists: List<PlaylistEntity>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_bottom_sheet, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cover: ImageView = itemView.findViewById(R.id.bs_playlist_cover)
        private val name: TextView = itemView.findViewById(R.id.bs_playlist_name)
        private val trackCount: TextView = itemView.findViewById(R.id.bs_playlist_track_count)

        fun bind(playlist: PlaylistEntity) {
            name.text = playlist.name


            val count = playlist.trackCount
            val trackString = when {
                count % 100 in 11..14 -> "$count треков"
                count % 10 == 1 -> "$count трек"
                count % 10 in 2..4 -> "$count трека"
                else -> "$count треков"
            }
            trackCount.text = trackString

            // Загрузка обложки
            if (playlist.coverPath != null) {
                Glide.with(itemView)
                    .load(File(playlist.coverPath))
                    .transform(CenterCrop(), RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.corner_radius_8)))
                    .into(cover)
            } else {
                Glide.with(itemView)
                    .load(R.drawable.placeholder_empty)
                    .transform(CenterCrop(), RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.corner_radius_8)))
                    .into(cover)
            }
        }
    }
}