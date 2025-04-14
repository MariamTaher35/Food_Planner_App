package com.example.foodplannerapplication.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.ActivityWelcomeBinding
import com.example.foodplannerapplication.utils.SessionManager

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var authManager: AuthManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        sessionManager = SessionManager(this)

        binding.guestButton.setOnClickListener {
            authManager.setGuestMode(true)
            authManager.setLoggedIn(false) //set logged in to false when guest mode is used.
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Check for existing login on start
        if (authManager.isLoggedIn() && !authManager.isGuestMode()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}