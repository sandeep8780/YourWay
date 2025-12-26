package com.example.yourway.userprofile

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)

    fun saveUserProfile(userProfile: UserProfile) {
        with(preferences.edit()) {
            putString("username", userProfile.username)
            putString("displayName", userProfile.displayName)
            putString("bio", userProfile.bio)
            putString("link", userProfile.link)
            putString("imgSrc", userProfile.imgSrc)
            apply()
        }
    }

    fun getUserProfile(): UserProfile? {
        val username = preferences.getString("username", null) ?: return null
        val displayName = preferences.getString("displayName", "")
        val bio = preferences.getString("bio", "")
        val link = preferences.getString("link", "")
        val imgSrc = preferences.getString("imgSrc", "")

        return UserProfile(username, displayName ?: "", bio ?: "", link ?: "", imgSrc ?: "")
    }

    fun isUserProfileAvailable(): Boolean {
        return preferences.contains("username") &&
                preferences.contains("displayName") &&
                preferences.contains("bio") &&
                preferences.contains("imgSrc")
    }
}
