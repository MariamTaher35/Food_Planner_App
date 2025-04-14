package com.example.foodplannerapplication.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "FoodPlannerPrefs"
        private const val KEY_USER_ID = "userId"
        const val GUEST_USER_ID = "guest_user" // Define the guest user ID constant
    }

    fun saveUserId(userId: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}