package com.example.foodplannerapplication.ViewModels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealOfDayViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = TheMealDBService.create()
    private val appDatabase = AppDatabase.getInstance(application.applicationContext)
    val authManager = AuthManager(application.applicationContext)

    private val _meal = MutableLiveData<Meal?>()
    val meal: LiveData<Meal?> = _meal

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    init {
        fetchRandomMeal()
    }

    private fun fetchRandomMeal() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val call = apiService.getRandomMeal()
            call.enqueue(object : Callback<MealsResponse> {
                override fun onResponse(
                    call: Call<MealsResponse>,
                    response: Response<MealsResponse>
                ) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _loading.value = false
                        if (response.isSuccessful) {
                            _meal.value = response.body()?.meals?.firstOrNull()
                            if (_meal.value == null) {
                                _error.value = "No meal found"
                            }
                        } else {
                            _error.value = "API request failed: ${response.message()}"
                        }
                    }
                }

                override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _loading.value = false
                        _error.value = "API request failed: ${t.message}"
                    }
                }
            })
        }
    }

    fun addMealToFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentMeal = _meal.value
                val currentUserId = userId
                if (currentUserId != null && currentMeal != null) {
                    val favoriteMeal = FavoriteMeal(
                        id = currentMeal.idMeal,
                        name = currentMeal.strMeal,
                        imageUrl = currentMeal.strMealThumb,
                        originCountry = null,
                        ingredients = null,
                        steps = null,
                        videoUrl = currentMeal.strYoutube,
                        guestMode = authManager.isGuestMode(),
                        userId = currentUserId
                    )
                    appDatabase.favoriteMealDao().insert(favoriteMeal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Added to Favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "User not logged in or Meal data not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MealOfDayViewModel", "Failed to add to Favorites: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Failed to add to Favorites: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun showPlanMealDialog() {
    }
}
