package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var themeSwitch: Switch

    companion object {
        private const val THEME_PREFS = "theme_prefs"
        private const val DARK_THEME_KEY = "dark_theme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applySavedTheme()

        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val shareButton = findViewById<LinearLayout>(R.id.share_button)
        val supportButton = findViewById<LinearLayout>(R.id.support_button)
        val userAgreementButton = findViewById<LinearLayout>(R.id.user_agreement_button)
        themeSwitch = findViewById<Switch>(R.id.theme_switch)

        themeSwitch.isChecked = prefs.getBoolean(DARK_THEME_KEY, false)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveThemePreference(isChecked)
            applyTheme(isChecked)
            recreate()
        }

        backButton.setOnClickListener {
            finish()
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.dev_group)
            )
            startActivity(Intent.createChooser(shareIntent, "Поделиться"))
        }

        supportButton.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.dev_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_theme))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))
            }
            startActivity(emailIntent)
        }

        userAgreementButton.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.agreement_url)))
            startActivity(browserIntent)
        }
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        prefs.edit()
            .putBoolean(DARK_THEME_KEY, isDarkTheme)
            .apply()
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

    private fun applyTheme(isDarkTheme: Boolean) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}