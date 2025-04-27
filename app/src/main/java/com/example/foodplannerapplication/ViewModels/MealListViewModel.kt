package com.example.foodplannerapplication.ViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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

class MealListViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = TheMealDBService.create()

    private val _meals = MutableLiveData<List<Meal>>()
    val meals: LiveData<List<Meal>> = _meals

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentCategory: String? = null
    private var currentArea: String? = null

    fun setFilters(category: String?, area: String?) {
        currentCategory = category
        currentArea = area
        fetchMeals()
    }

    private fun fetchMeals() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val call: Call<MealsResponse> = if (currentCategory != null) {
                apiService.getMealsByCategory(currentCategory!!)
            } else if (currentArea != null) {
                apiService.getMealsByArea(currentArea!!)
            } else {
                // Handle the case where both are null (optional, depending on your app's logic)
                _error.postValue("Category or Area must be provided")
                _loading.postValue(false)
                return@launch
            }

            call.enqueue(object : Callback<MealsResponse> {
                override fun onResponse(
                    call: Call<MealsResponse>,
                    response: Response<MealsResponse>
                ) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _loading.value = false
                        if (response.isSuccessful) {
                            _meals.value = response.body()?.meals ?: emptyList()
                            _error.value = null
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
}
