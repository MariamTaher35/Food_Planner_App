package com.example.foodplannerapplication.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodplannerapplication.Adapters.PlannedMealsAdapter
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.FragmentPlanBinding
import com.example.foodplannerapplication.utils.CalendarManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanFragment : Fragment() {

    private var _binding: FragmentPlanBinding? = null
    private val binding get() = _binding!!
    private lateinit var appDatabase: AppDatabase
    private lateinit var plannedMealsAdapter: PlannedMealsAdapter
    private lateinit var authManager: AuthManager
    private lateinit var guestMessageTextView: TextView
    private lateinit var calendarManager: CalendarManager
    private var mostRecentMealToAdd: PlannedMeal? = null
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val calendarPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val writeGranted = permissions.getOrDefault(Manifest.permission.WRITE_CALENDAR, false)
            val readGranted = permissions.getOrDefault(Manifest.permission.READ_CALENDAR, false)

            Log.d("CalendarPermission", "Write Granted: $writeGranted, Read Granted: $readGranted")

            if (writeGranted && readGranted) {
                mostRecentMealToAdd?.let { calendarManager.addMealToCalendar(it) }
                mostRecentMealToAdd = null
            } else {
                Toast.makeText(
                    requireContext(),
                    "Calendar write and read permissions are required.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPlanBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getInstance(requireContext())
        authManager = AuthManager(requireContext())
        calendarManager = CalendarManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        guestMessageTextView = view.findViewById(R.id.guest_mode_message)

        if (authManager.isGuestMode()) {
            binding.plannedMealsRecyclerView.visibility = View.GONE
            guestMessageTextView.visibility = View.VISIBLE
        } else {
            binding.plannedMealsRecyclerView.visibility = View.VISIBLE
            guestMessageTextView.visibility = View.GONE

            setupRecyclerView()
            observePlannedMeals()
        }
    }

    private fun checkCalendarPermissionAndAdd(meal: PlannedMeal) {
        val writePermissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_CALENDAR
        )
        val readPermissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CALENDAR
        )

        Log.d("PlanFragment", "Write Permission Status: $writePermissionStatus")
        Log.d("PlanFragment", "Read Permission Status: $readPermissionStatus")

        if (writePermissionStatus == PackageManager.PERMISSION_GRANTED &&
            readPermissionStatus == PackageManager.PERMISSION_GRANTED
        ) {
            calendarManager.addMealToCalendar(meal)
        } else {
            calendarPermissionResult.launch(
                arrayOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR)
            )
        }
    }

    private fun setupRecyclerView() {
        plannedMealsAdapter = PlannedMealsAdapter { meal ->
            deletePlannedMeal(meal)
        }
        plannedMealsAdapter.onMealClick = { meal ->  // Set the click listener here
            navigateToMealDetails(meal.id) // Access mealId here
        }
        binding.plannedMealsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.plannedMealsRecyclerView.adapter = plannedMealsAdapter
    }

    private fun observePlannedMeals() {
        userId?.let { currentUserId ->
            lifecycleScope.launch {
                appDatabase.plannedMealDao().getAll(currentUserId).collectLatest { meals ->
                    withContext(Dispatchers.Main) {
                        plannedMealsAdapter.submitList(meals)
                        meals.forEach { meal ->
                            mostRecentMealToAdd = meal
                            checkCalendarPermissionAndAdd(meal)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMealDetails(mealId: String) {
        val bundle = Bundle().apply {
            putString("mealId", mealId)
        }
        findNavController().navigate(R.id.mealDetailsFragment, bundle)
    }

    private fun deletePlannedMeal(plannedMeal: PlannedMeal) {
        userId?.let { currentUserId ->
            lifecycleScope.launch(Dispatchers.IO) {
                if (plannedMeal.userId == currentUserId) {
                    appDatabase.plannedMealDao().delete(plannedMeal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Meal deleted from plan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSearchDialog() {
        val searchEditText = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Search for a Meal")
            .setView(searchEditText)
            .setPositiveButton("Search") { _, _ ->
                val searchTerm = searchEditText.text.toString().trim()
                if (searchTerm.isNotEmpty()) {
                    performSearchAndAddMeal(searchTerm)
                } else {
                    Toast.makeText(requireContext(), "Please enter a search term.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSearchAndAddMeal(searchTerm: String) {
        lifecycleScope.launch {
            delay(1000)
            val dummyMealId = "searchedMealId_${System.currentTimeMillis()}"
            val dummyMealName = "Searched Meal: $searchTerm"
            val dummyMealImageUrl = "https://example.com/searched_meal.jpg"
            withContext(Dispatchers.Main) {
                showDayAndTimePickerDialog(dummyMealId, dummyMealName, dummyMealImageUrl)
            }
        }
    }

    private fun showDayAndTimePickerDialog(mealId: String, mealName: String, mealImageUrl: String) {
        val daysOfWeek = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        var selectedDay = daysOfWeek[0]
        var selectedHour = 12
        var selectedMinute = 0

        val dayDialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Day of the Week")
            .setSingleChoiceItems(daysOfWeek, 0) { _, which ->
                selectedDay = daysOfWeek[which]
                Log.d("PlanFragment", "Selected Day: $selectedDay")
            }
            .setPositiveButton("Next") { _, _ ->
                val timePickerDialog = android.app.TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        selectedHour = hourOfDay
                        selectedMinute = minute
                        val mealTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                        Log.d("PlanFragment", "Selected Time: $mealTime")
                        addMealToPlan(mealId, selectedDay, mealTime, mealName, mealImageUrl)
                    },
                    selectedHour,
                    selectedMinute,
                    true // 24-hour format
                )
                timePickerDialog.show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dayDialog.show()
    }

    private fun addMealToPlan(mealId: String, dayOfWeek: String, mealTime: String, mealName: String, mealImageUrl: String) {
        userId?.let { currentUserId ->
            val plannedMeal = PlannedMeal(mealId, dayOfWeek, mealTime, authManager.isGuestMode(), mealName, mealImageUrl, currentUserId)
            Log.d("PlanFragment", "Creating PlannedMeal with userId: $currentUserId")

            lifecycleScope.launch(Dispatchers.IO) {
                appDatabase.plannedMealDao().insert(plannedMeal)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "$mealName added to plan for $dayOfWeek at $mealTime", Toast.LENGTH_SHORT).show()
                    mostRecentMealToAdd = plannedMeal
                    checkCalendarPermissionAndAdd(plannedMeal)
                }
            }
        } ?: run {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
