package com.example.batique

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference

class SettingActivity : AppCompatActivity(), Preference.OnPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout_setting, SettingFragment())
            .commit()

        val actionbar = supportActionBar
        actionbar?.title = getString(R.string.theme)
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference.key == "theme_preference") {
            val selectedTheme = newValue as String
            when (selectedTheme) {
                "Otomatis (Mengikuti tema perangkat)" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                "Gelap" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            // Recreate the activity to apply the new theme
            recreate()
            // Update the summary of the preference based on the selected theme
            preference.summary = selectedTheme
            return true
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}