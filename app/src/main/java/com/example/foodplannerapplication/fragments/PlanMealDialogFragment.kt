package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.DialogPlanMealBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanMealDialogFragment : DialogFragment() {

    private var _binding: DialogPlanMealBinding? = null
    private val binding get() = _binding!!
    private lateinit var appDatabase: AppDatabase
    private lateinit var authManager: AuthManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogPlanMealBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getInstance(requireContext())
        authManager = AuthManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mealName = arguments?.getString("mealName") ?: ""
        val mealImageUrl = arguments?.getString("mealImageUrl") ?: ""
        val mealId = arguments?.getString("mealId") ?: ""

        binding.mealNameTextView.text = mealName
        Glide.with(requireContext()).load(mealImageUrl).into(binding.mealImageView)

        val dayOfWeekAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.days_of_week,
            android.R.layout.simple_spinner_item
        )
        dayOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dayOfWeekSpinner.adapter = dayOfWeekAdapter

        binding.addToPlanButton.setOnClickListener {
            val dayOfWeek = binding.dayOfWeekSpinner.selectedItem.toString()
            val mealTime = binding.mealTimeEditText.text.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

            if (userId != null) {
                val plannedMeal = PlannedMeal(
                    mealId,
                    dayOfWeek,
                    mealTime,
                    authManager.isGuestMode(),
                    mealName,
                    mealImageUrl,
                    userId // Set the userId here!
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    appDatabase.plannedMealDao().insert(plannedMeal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Meal added to plan", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            } else {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}