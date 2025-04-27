package com.example.foodplannerapplication.ViewModels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.utils.CalendarManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanViewModel(application: Application) : AndroidViewModel(application) {

    private val appDatabase = AppDatabase.getInstance(application)
    val authManager = AuthManager(application)
    val calendarManager = CalendarManager(application)

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val _plannedMeals = MutableLiveData<List<PlannedMeal>>()
    val plannedMeals: LiveData<List<PlannedMeal>> = _plannedMeals

    private val _showSearchDialog = MutableLiveData<Boolean>()
    val showSearchDialog: LiveData<Boolean> = _showSearchDialog

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private var mostRecentMealToAdd: PlannedMeal? = null

    // Use StateFlow for ongoing observation and caching
    val plannedMealsFlow: StateFlow<List<PlannedMeal>> = userId?.let { currentUserId ->
        appDatabase.plannedMealDao().getAll(currentUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // Keep data for 5 seconds if no observers
                initialValue = emptyList()
            )
    } ?: run {
        // Return a StateFlow that emits an empty list if there is no user.
        emptyFlow<List<PlannedMeal>>()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    }


    init {
        loadPlannedMeals()
    }

    fun loadPlannedMeals() {
        userId?.let { currentUserId ->
            viewModelScope.launch(Dispatchers.IO) {
                appDatabase.plannedMealDao().getAllOnce(currentUserId).let { meals ->
                    withContext(Dispatchers.Main) {
                        _plannedMeals.value = meals
                    }
                }
            }
        }
    }

    fun deletePlannedMeal(plannedMeal: PlannedMeal) {
        userId?.let { currentUserId ->
            viewModelScope.launch(Dispatchers.IO) {
                if (plannedMeal.userId == currentUserId) {
                    appDatabase.plannedMealDao().delete(plannedMeal)
                    withContext(Dispatchers.Main) {
                        _toastMessage.value = "Meal deleted from plan"
                        loadPlannedMeals() // Refresh the list after deletion
                    }
                }
            }
        }
    }

    fun showSearchDialog() {
        _showSearchDialog.value = true
    }

    fun onSearchDialogShown() {
        _showSearchDialog.value = false
    }

    fun addMealToPlan(mealId: String, dayOfWeek: String, mealTime: String, mealName: String, mealImageUrl: String) {
        userId?.let { currentUserId ->
            val plannedMeal = PlannedMeal(mealId, dayOfWeek, mealTime, authManager.isGuestMode(), mealName, mealImageUrl, currentUserId)
            mostRecentMealToAdd = plannedMeal // Store for calendar
            viewModelScope.launch(Dispatchers.IO) {
                appDatabase.plannedMealDao().insert(plannedMeal)
                withContext(Dispatchers.Main) {
                    _toastMessage.value = "$mealName added to plan for $dayOfWeek at $mealTime"
                }
            }
        } ?: run {
            _toastMessage.value = "User not logged in."
        }
    }

    fun checkCalendarPermissionAndAdd(meal: PlannedMeal) {
        //  The ViewModel should not directly interact with UI elements like Dialogs.
        //  Instead, you can use LiveData to communicate to the Fragment that the dialog should be shown.
        //  For now,  leave this empty and handle the dialog in the fragment.
    }

    fun onCalendarPermissionResult(granted: Boolean) {
        if (granted) {
            mostRecentMealToAdd?.let {
                calendarManager.addMealToCalendar(it)
                mostRecentMealToAdd = null // Clear after adding
            }
        } else {
            _toastMessage.value = "Calendar permissions are required to add meals to your calendar."
        }
    }

    fun navigateToMealDetails(mealId: String) {
        //  The ViewModel should not directly handle navigation.
        //  Instead, use LiveData to communicate to the Fragment that navigation should occur.
    }
}

