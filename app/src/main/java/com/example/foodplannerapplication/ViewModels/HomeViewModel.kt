package com.example.foodplannerapplication.ViewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Adapters.CategoryAdapter
import com.example.foodplannerapplication.Models.Category
import com.example.foodplannerapplication.Models.CategoryResponse
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.utils.Event
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = TheMealDBService.create()

    private val _mealOfDay = MutableLiveData<Meal?>()
    val mealOfDay: LiveData<Meal?> = _mealOfDay

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _recentlyViewedMeals = MutableLiveData<List<Meal>>()
    val recentlyViewedMeals: LiveData<List<Meal>> = _recentlyViewedMeals

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val sharedPrefsKey = "recently_viewed_meals"
    private val context: Context = application.applicationContext

    private val _navigateToMealList = MutableLiveData<Event<Pair<String, String>>>()
    val navigateToMealList: LiveData<Event<Pair<String, String>>> = _navigateToMealList

    val categoryAdapter = CategoryAdapter(emptyList()) { category ->
        navigateToMealList("category", category.strCategory)
    }

    init {
        fetchMealOfDay()
        fetchCategories()
        loadRecentlyViewedMeals()
    }

    private fun navigateToMealList(filterType: String, filterValue: String) {
        _navigateToMealList.value = Event(filterType to filterValue)
    }

    private fun fetchMealOfDay() {
        _loading.value = true
        apiService.getRandomMeal().enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                if (response.isSuccessful) {
                    _mealOfDay.value = response.body()?.meals?.firstOrNull()
                    _mealOfDay.value?.let {
                        saveToRecentlyViewed(it)
                    }
                    _loading.value = false
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch meal of the day: ${response.message()}"
                    _loading.value = false
                    _mealOfDay.value = null
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                _error.value = "Error fetching meal of the day: ${t.message}"
                _loading.value = false
                _mealOfDay.value = null
                Log.e("HomeViewModel", "Error fetching meal of the day: ${t.message}")
            }
        })
    }

    private fun fetchCategories() {
        _loading.value = true
        apiService.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                if (response.isSuccessful) {
                    _categories.value = response.body()?.categories ?: emptyList()
                    _loading.value = false
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch categories: ${response .message()}"
                    _loading.value = false
                    _categories.value = emptyList()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                _error.value = "Error fetching categories: ${t.message}"
                _loading.value = false
                _categories.value = emptyList()
                Log.e("HomeViewModel", "Error fetching categories: ${t.message}")
            }
        })
    }

    private fun loadRecentlyViewedMeals() {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPrefs = context.getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPrefs.getString("recently_viewed", null)
            val type = object : TypeToken<List<Meal>>() {}.type
            val loadedList: List<Meal> =
                if (json != null) gson.fromJson(json, type) ?: emptyList() else emptyList()
            withContext(Dispatchers.Main) {
                _recentlyViewedMeals.value = loadedList
            }
        }
    }

    private fun saveToRecentlyViewed(meal: Meal) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPrefs = context.getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            val gson = Gson()

            // Load existing list
            val existingJson = sharedPrefs.getString("recently_viewed", null)
            val type: Type = object : TypeToken<List<Meal>>() {}.type
            val existingList: MutableList<Meal> =
                if (existingJson != null) gson.fromJson(existingJson, type) ?: mutableListOf() else mutableListOf()

            // Check if the meal is already in the list
            val mealIndex = existingList.indexOfFirst { it.idMeal == meal.idMeal }
            if (mealIndex != -1) {
                existingList.removeAt(mealIndex)
            }
            // Add the new meal to the front
            existingList.add(0, meal)
            // Keep only the last 'maxRecentlyViewed' meals
            val maxRecentlyViewed = 10 // Or any appropriate value for your logic
            if (existingList.size > maxRecentlyViewed) {
                val subList = existingList.subList(maxRecentlyViewed, existingList.size)
                subList.clear()
            }

            val json = gson.toJson(existingList)
            editor.putString("recently_viewed", json)
            editor.apply()
            withContext(Dispatchers.Main) {
                _recentlyViewedMeals.value = existingList
            }
        }
    }

    // Adapter update functions
    fun updateCategories(newCategories: List<Category>) {
        _categories.value = newCategories
    }
}