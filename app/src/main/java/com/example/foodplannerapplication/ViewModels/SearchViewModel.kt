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

    fun searchMeals(searchTerm: String) {
        viewModelScope.launch(Dispatchers.IO) {
            apiService.searchMeals(searchTerm).enqueue(object : Callback<MealsResponse> {
                override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                    if (response.isSuccessful) {
                        val meals = response.body()?.meals ?: emptyList()
                        mealsLiveData.postValue(meals)
                    } else {
                        errorMessage.postValue("API request failed: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                    errorMessage.postValue("API request failed: ${t.message}")
                }
            })
        }
    }
}