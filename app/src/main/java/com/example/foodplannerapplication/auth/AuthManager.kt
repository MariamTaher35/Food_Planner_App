package com.example.foodplannerapplication.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class AuthManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_GUEST_MODE = "is_guest_mode"
    }

    fun setLoggedIn(loggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setGuestMode(isGuest: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_GUEST_MODE, isGuest).apply()
    }

    fun isGuestMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_GUEST_MODE, false)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

}