package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.DialogPlanMealBinding
import com.example.foodplannerapplication.viewmodels.PlanMealDialogViewModel
import kotlinx.coroutines.launch

class PlanMealDialogFragment : DialogFragment() {

    private var _binding: DialogPlanMealBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlanMealDialogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogPlanMealBinding.inflate(inflater, container, false)
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

            viewModel.addMealToPlan(mealId, dayOfWeek, mealTime, mealName, mealImageUrl,
                onSuccess = {
                    Toast.makeText(context, "Meal added to plan", Toast.LENGTH_SHORT).show()
                    dismiss()
                },
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
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