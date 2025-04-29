package com.example.foodplannerapplication.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = TheMealDBService.create()
    val mealsLiveData = MutableLiveData<List<Meal>>()
    val errorMessage = MutableLiveData<String>()

    // Search by meal name
    fun searchByName(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            apiService.searchMeals(query).enqueue(object : Callback<MealsResponse> {
                override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                    if (response.isSuccessful) {
                        mealsLiveData.postValue(response.body()?.meals ?: emptyList())
                    } else {
                        errorMessage.postValue("Failed to fetch meals by name: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                    errorMessage.postValue("Error fetching meals by name: ${t.message}")
                }
            })
        }
    }

    // Search by category
    fun searchByCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            apiService.getMealsByCategory(category).enqueue(object : Callback<MealsResponse> {
                override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                    if (response.isSuccessful) {
                        mealsLiveData.postValue(response.body()?.meals ?: emptyList())
                    } else {
                        errorMessage.postValue("Failed to fetch by category: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                    errorMessage.postValue("Error fetching meals by category: ${t.message}")
                }
            })
        }
    }

    // Search by area (country)
    fun searchByArea(area: String) {
        viewModelScope.launch(Dispatchers.IO) {
            apiService.getMealsByArea(area).enqueue(object : Callback<MealsResponse> {
                override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                    if (response.isSuccessful) {
                        mealsLiveData.postValue(response.body()?.meals ?: emptyList())
                    } else {
                        errorMessage.postValue("Failed to fetch by area: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                    errorMessage.postValue("Error fetching meals by area: ${t.message}")
                }
            })
        }
    }
}
