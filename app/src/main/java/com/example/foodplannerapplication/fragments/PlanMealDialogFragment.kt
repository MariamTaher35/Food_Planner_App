package com.example.foodplannerapplication.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.PlanMealDialogViewModel
import com.example.foodplannerapplication.databinding.DialogPlanMealBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlanMealDialogFragment : DialogFragment() {

    private var _binding: DialogPlanMealBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlanMealDialogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogPlanMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use requireArguments() to ensure arguments are present
        val mealName = requireArguments().getString("mealName")
        val mealImageUrl = requireArguments().getString("mealImageUrl")
        val mealId = requireArguments().getString("mealId")

        binding.mealNameTextView.text = mealName
        Glide.with(requireContext()).load(mealImageUrl).into(binding.mealImageView)

        // Use the application context to avoid leaks.
        val dayOfWeekAdapter = ArrayAdapter.createFromResource(
            requireContext().applicationContext, // Use application context
            R.array.days_of_week,
            android.R.layout.simple_spinner_item
        )
        dayOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dayOfWeekSpinner.adapter = dayOfWeekAdapter

        binding.addToPlanButton.setOnClickListener {
            val dayOfWeek = binding.dayOfWeekSpinner.selectedItem.toString()
            val mealTime = binding.mealTimeEditText.text.toString()

            if (mealId != null) {
                if (mealName != null) {
                    if (mealImageUrl != null) {
                        viewModel.addMealToPlan(
                            mealId, dayOfWeek, mealTime, mealName, mealImageUrl,
                            onSuccess = {
                                // Use getString() for string resources
                                Toast.makeText(requireContext(), getString(R.string.meal_added_to_plan), Toast.LENGTH_SHORT).show()
                                dismiss()
                            },
                            onError = { errorMessage: String ->
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        binding.mealTimeEditText.setOnClickListener {
            // Inflate the custom layout for the time picker
            val dialogView = layoutInflater.inflate(R.layout.custom_time_picker_dialog, null)
            val timePicker = dialogView.findViewById<TimePicker>(R.id.time_picker)
            val setButton = dialogView.findViewById<Button>(R.id.set_button)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)

            // Create a new dialog
            val timePickerDialog = Dialog(requireContext())
            timePickerDialog.setContentView(dialogView)

            // Set the current time in the TimePicker
            val calendar = Calendar.getInstance()
            timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(Calendar.MINUTE)

            // Set button click listener
            setButton.setOnClickListener {
                val selectedHour = timePicker.hour
                val selectedMinute = timePicker.minute
                val calendarInstance = Calendar.getInstance()
                calendarInstance.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendarInstance.set(Calendar.MINUTE, selectedMinute)
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())  // "h:mm a" for 12-hour format
                val formattedTime = sdf.format(calendarInstance.time)
                binding.mealTimeEditText.setText(formattedTime)
                timePickerDialog.dismiss()
            }

            // Cancel button click listener
            cancelButton.setOnClickListener {
                timePickerDialog.dismiss()
            }

            // Show the custom dialog
            timePickerDialog.show()

            // Set the width of the dialog to 90% of the screen width
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt() // 90% of screen width
            timePickerDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
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

