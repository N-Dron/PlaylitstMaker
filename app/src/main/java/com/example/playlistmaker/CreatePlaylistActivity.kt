package com.example.playlistmaker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.db.PlaylistEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistActivity : AppCompatActivity() {

    private lateinit var playlistCover: ImageView
    private lateinit var nameEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var backButton: ImageButton

    private var selectedImageUri: Uri? = null
    private lateinit var database: AppDatabase

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius_8)))
                .into(playlistCover)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)

        database = AppDatabase.getDatabase(this)

        playlistCover = findViewById(R.id.playlist_cover)
        nameEditText = findViewById(R.id.name_edit_text)
        descriptionEditText = findViewById(R.id.description_edit_text)
        createButton = findViewById(R.id.create_button)
        backButton = findViewById(R.id.back_button)

        setupListeners()
        setupBackPressHandling()
    }

    private fun setupListeners() {
        playlistCover.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createButton.isEnabled = !s.isNullOrBlank()

                if (createButton.isEnabled) {
                    createButton.setBackgroundColor(getColor(R.color.accent))
                } else {
                    createButton.setBackgroundColor(getColor(R.color.light_gray))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        createButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            val coverPath = selectedImageUri?.let { saveImageToPrivateStorage(it) }

            // Создаем сущность плейлиста
            val playlist = PlaylistEntity(
                name = name,
                description = description,
                coverPath = coverPath,
                trackIds = "[]",
                trackCount = 0
            )

            // Сохраняем в БД
            lifecycleScope.launch {
                database.playlistDao().insertPlaylist(playlist)
                Toast.makeText(this@CreatePlaylistActivity, "Плейлист $name создан", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Клик по кнопке "Назад"
        backButton.setOnClickListener {
            checkUnsavedDataAndExit()
        }
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkUnsavedDataAndExit()
            }
        })
    }

    private fun checkUnsavedDataAndExit() {
        val hasName = !nameEditText.text.isNullOrBlank()
        val hasDescription = !descriptionEditText.text.isNullOrBlank()
        val hasCover = selectedImageUri != null

        if (hasName || hasDescription || hasCover) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Завершить создание плейлиста?")
                .setMessage("Все несохраненные данные будут потеряны")
                .setNeutralButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Завершить") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .show()
        } else {
            finish()
        }
    }

    // Сохранение картинки во внутреннее хранилище приложения
    private fun saveImageToPrivateStorage(uri: Uri): String {
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "playlist_covers")
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        BitmapFactory.decodeStream(inputStream)?.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

        inputStream?.close()
        outputStream.close()

        return file.absolutePath
    }
}