package com.example.foodplannerapplication.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Models.Category
import com.example.foodplannerapplication.Models.CategoryResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val apiService = TheMealDBService.create()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val call = apiService.getCategories()
            call.enqueue(object : Callback<CategoryResponse> {
                override fun onResponse(
                    call: Call<CategoryResponse>,
                    response: Response<CategoryResponse>
                ) {
                    viewModelScope.launch(Dispatchers.Main) { // Switch to main thread for updating LiveData
                        _loading.value = false
                        if (response.isSuccessful) {
                            val categories = response.body()?.categories ?: emptyList()
                            _categories.value = categories
                        } else {
                            _error.value = "API request failed: ${response.message()}"
                        }
                    }
                }

                override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _loading.value = false
                        _error.value = "API request failed: ${t.message}"
                    }
                }
            })
        }
    }
}
