package com.example.foodplannerapplication.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Models.Area
import com.example.foodplannerapplication.Models.AreaResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CountriesViewModel : ViewModel() {

    private val _countries = MutableLiveData<List<Area>>()
    val countries: LiveData<List<Area>> = _countries

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val apiService = TheMealDBService.create()

    init {
        fetchCountries()
    }

    fun fetchCountries() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) { // Network call on IO thread
            val call = apiService.getAreas()
            call.enqueue(object : Callback<AreaResponse> {
                override fun onResponse(call: Call<AreaResponse>, response: Response<AreaResponse>) {
                    viewModelScope.launch(Dispatchers.Main) { // Update LiveData on main thread
                        _loading.value = false
                        if (response.isSuccessful) {
                            val areaResponse = response.body()
                            _countries.value = areaResponse?.meals ?: emptyList()
                        } else {
                            _error.value = "Failed to fetch countries: ${response.message()}"
                        }
                    }
                }

                override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                    viewModelScope.launch(Dispatchers.Main) { // Update LiveData on main thread
                        _loading.value = false
                        _error.value = "Error fetching countries: ${t.message}"
                    }
                }
            })
        }
    }
}
