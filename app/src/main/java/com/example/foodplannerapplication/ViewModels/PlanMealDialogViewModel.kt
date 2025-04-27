package com.example.foodplannerapplication.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanMealDialogViewModel(application: Application) : AndroidViewModel(application) {

    private val appDatabase = AppDatabase.getInstance(application)
    private val authManager = AuthManager(application)

    fun addMealToPlan(mealId: String, dayOfWeek: String, mealTime: String, mealName: String, mealImageUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid

        if (userId != null) {
            val plannedMeal = PlannedMeal(
                mealId,
                dayOfWeek,
                mealTime,
                authManager.isGuestMode(),
                mealName,
                mealImageUrl,
                userId
            )

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    appDatabase.plannedMealDao().insert(plannedMeal)
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onError("Failed to add meal: ${e.message}")
                    }
                }
            }
        } else {
            onError("User  not logged in")
        }
    }
}