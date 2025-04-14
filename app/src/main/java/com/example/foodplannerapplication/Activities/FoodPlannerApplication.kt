package com.example.foodplannerapplication.Activities

import android.app.Application
import com.pierfrancescosoffritti.androidyoutubeplayer.BuildConfig

import timber.log.Timber

class FoodPlannerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}