package com.example.playlistmaker

import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.db.PlaylistEntity
import com.example.playlistmaker.db.TrackDbConvertor
import com.example.playlistmaker.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        private const val UPDATE_TIMER_DELAY = 300L
    }

    private var playerState = STATE_DEFAULT
    private var mediaPlayer = MediaPlayer()

    private lateinit var playButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var playbackTime: TextView

    private lateinit var database: AppDatabase
    private val convertor = TrackDbConvertor()
    private var isFavorite = false

    private val gson = Gson()

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                playbackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
                handler.postDelayed(this, UPDATE_TIMER_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val track = intent.getSerializableExtra("EXTRA_TRACK") as? Track ?: return

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val coverImage = findViewById<ImageView>(R.id.cover_image)
        val trackName = findViewById<TextView>(R.id.track_name)
        val artistName = findViewById<TextView>(R.id.artist_name)
        val durationValue = findViewById<TextView>(R.id.duration_value)
        val albumGroup = findViewById<Group>(R.id.album_group)
        val albumValue = findViewById<TextView>(R.id.album_value)
        val yearValue = findViewById<TextView>(R.id.year_value)
        val genreValue = findViewById<TextView>(R.id.genre_value)
        val countryValue = findViewById<TextView>(R.id.country_value)

        playButton = findViewById(R.id.play_button)
        favoriteButton = findViewById(R.id.favorite_button)
        playbackTime = findViewById(R.id.playback_time)

        database = AppDatabase.getDatabase(this)

        favoriteButton.setImageResource(R.drawable.ic_favorite_border)

        trackName.text = track.trackName
        artistName.text = track.artistName
        durationValue.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        if (track.collectionName.isNullOrEmpty()) {
            albumGroup.visibility = View.GONE
        } else {
            albumGroup.visibility = View.VISIBLE
            albumValue.text = track.collectionName
        }
        yearValue.text = track.releaseDate?.take(4) ?: ""
        genreValue.text = track.primaryGenreName
        countryValue.text = track.country

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.rounded_placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius_8)))
            .into(coverImage)

        preparePlayer(track.previewUrl)

        playButton.setOnClickListener {
            playbackControl()
        }

        backButton.setOnClickListener {
            finish()
        }


        lifecycleScope.launch {
            database.trackDao().getFavoriteTrackIds().collect { ids ->
                isFavorite = ids.contains(track.trackId)
                updateFavoriteButton()
            }
        }

        favoriteButton.setOnClickListener {
            lifecycleScope.launch {
                if (isFavorite) {
                    database.trackDao().deleteTrack(convertor.map(track))
                } else {
                    database.trackDao().insertTrack(convertor.map(track))
                }
            }
        }


        val addToPlaylistButton = findViewById<ImageButton>(R.id.add_to_playlist_button)
        val bottomSheetContainer = findViewById<LinearLayout>(R.id.playlists_bottom_sheet)
        val overlay = findViewById<View>(R.id.overlay)
        val bsNewPlaylistButton = findViewById<Button>(R.id.bs_new_playlist_button)
        val bsRecyclerView = findViewById<RecyclerView>(R.id.bs_recycler_view)

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> overlay.visibility = View.GONE
                    else -> overlay.visibility = View.VISIBLE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        addToPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bsNewPlaylistButton.setOnClickListener {
            val intent = Intent(this, CreatePlaylistActivity::class.java)
            startActivity(intent)
        }

        // Настройка списка плейлистов в шторке
        val bsAdapter = BottomSheetPlaylistAdapter(emptyList()) { playlist ->
            addTrackToPlaylist(playlist, track, bottomSheetBehavior)
        }
        bsRecyclerView.layoutManager = LinearLayoutManager(this)
        bsRecyclerView.adapter = bsAdapter

        // Загрузка плейлистов из БД
        lifecycleScope.launch {
            database.playlistDao().getPlaylists().collect { playlists ->
                bsAdapter.updatePlaylists(playlists)
            }
        }
    }

    // Метод добавления трека в плейлист
    private fun addTrackToPlaylist(playlist: PlaylistEntity, track: Track, behavior: BottomSheetBehavior<LinearLayout>) {
        val type = object : TypeToken<MutableList<Long>>() {}.type


        val trackIds: MutableList<Long> = if (playlist.trackIds.isBlank() || playlist.trackIds == "[]") {
            mutableListOf()
        } else {
            gson.fromJson(playlist.trackIds, type)
        }


        if (trackIds.contains(track.trackId)) {
            Toast.makeText(this, "Трек уже добавлен в плейлист ${playlist.name}", Toast.LENGTH_SHORT).show()
        } else {
            trackIds.add(track.trackId)
            val updatedPlaylist = playlist.copy(
                trackIds = gson.toJson(trackIds),
                trackCount = trackIds.size
            )

            lifecycleScope.launch {
                database.playlistDao().updatePlaylist(updatedPlaylist)
                Toast.makeText(this@AudioPlayerActivity, "Добавлено в плейлист ${playlist.name}", Toast.LENGTH_SHORT).show()
                behavior.state = BottomSheetBehavior.STATE_HIDDEN // Закрываем шторку
            }
        }
    }

    private fun updateFavoriteButton() {
        val colorRes = if (isFavorite) {
            R.color.pink
        } else {
            R.color.icon_tint
        }
        val color = ContextCompat.getColor(this, colorRes)
        favoriteButton.imageTintList = ColorStateList.valueOf(color)
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playButton.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            playerState = STATE_PREPARED
            playButton.setImageResource(R.drawable.ic_play)
            handler.removeCallbacks(updateTimerRunnable)
            playbackTime.text = getString(R.string.playback_time_start)
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setImageResource(R.drawable.ic_pause)
        playerState = STATE_PLAYING
        handler.post(updateTimerRunnable)
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setImageResource(R.drawable.ic_play)
        playerState = STATE_PAUSED
        handler.removeCallbacks(updateTimerRunnable)
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimerRunnable)
        mediaPlayer.release()
    }
}