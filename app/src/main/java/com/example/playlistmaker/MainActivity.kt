package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {

    companion object {
        private const val THEME_PREFS = "theme_prefs"
        private const val DARK_THEME_KEY = "dark_theme"
        private const val REQUEST_CODE_SETTINGS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<MaterialButton>(R.id.search_button)
        val mediaButton = findViewById<MaterialButton>(R.id.media_button)
        val settingsButton = findViewById<MaterialButton>(R.id.settings_button)

        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        mediaButton.setOnClickListener {
            val intent = Intent(this, MediaLibraryActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SETTINGS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTINGS) {
            checkAndApplyTheme()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndApplyTheme()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
        val isDarkTheme = prefs.getBoolean(DARK_THEME_KEY, false)

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun checkAndApplyTheme() {
        val prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
        val isDarkTheme = prefs.getBoolean(DARK_THEME_KEY, false)

        val currentTheme = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                val nightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        if (isDarkTheme != currentTheme) {
            applySavedTheme()
            recreate()
        }
    }
}