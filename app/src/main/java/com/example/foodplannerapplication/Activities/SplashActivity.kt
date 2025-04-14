package com.example.foodplannerapplication.Activities

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.example.foodplannerapplication.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val animationView = findViewById<LottieAnimationView>(R.id.animationView)

        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Do nothing
            }

            override fun onAnimationEnd(animation: Animator) {
                // Start the Welcome activity instead of Login Activity
                startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                finish() // Finish the splash activity to prevent going back to it
            }

            override fun onAnimationCancel(animation: Animator) {
                // Do nothing
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Do nothing
            }
        })
    }
}