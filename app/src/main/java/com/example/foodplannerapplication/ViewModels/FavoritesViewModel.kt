package com.example.foodplannerapplication.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.FavoriteMeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class FavoritesViewModel(private val appDatabase: AppDatabase, private val userId: String?) : ViewModel() {

    private val _favoriteMeals = MutableLiveData<List<FavoriteMeal>>()
    val favoriteMeals: LiveData<List<FavoriteMeal>> = _favoriteMeals

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        if (userId == null) {
            _message.value = "User not logged in"
            return
        }
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.favoriteMealDao().getAll(userId).collectLatest { meals ->
                withContext(Dispatchers.Main) {
                    _favoriteMeals.value = meals
                    _loading.value = false
                }
            }
        }
    }

    fun deleteFavoriteMeal(meal: FavoriteMeal) {
        if (userId == null) {
            _message.value = "User not logged in"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (meal.userId == userId) { // added null check
                appDatabase.favoriteMealDao().delete(meal)
                withContext(Dispatchers.Main) {
                    _message.value = "${meal.name} removed from favorites"
                    loadFavorites() // Refresh the list after deletion
                }
            }
        }
    }
}
