package com.example.batique

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

        // Set the preference change listener
        val themePreference = findPreference<Preference>("theme_preference")
        themePreference?.onPreferenceChangeListener = (activity as SettingActivity)

        // Set the initial summary of the preference based on the selected theme
        val selectedTheme = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("theme_preference", "Terang")
        themePreference?.summary = selectedTheme
    }
}