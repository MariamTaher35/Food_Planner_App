
package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodplannerapplication.Adapters.MealAdapter
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.MealListViewModel
import com.example.foodplannerapplication.databinding.FragmentMealListBinding

class MealListFragment : Fragment() {

    private var _binding: FragmentMealListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MealListViewModel by viewModels()
    private lateinit var mealAdapter: MealAdapter
    private lateinit var loadingView: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var rootView: FrameLayout // Root view to add loading/error views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MealListFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("MealListFragment", "onCreateView")
        _binding = FragmentMealListBinding.inflate(inflater, container, false)
        // Create a FrameLayout to serve as the root for this fragment.
        rootView = FrameLayout(requireContext())
        // Inflate the RecyclerView into the FrameLayout.
        val recyclerView = binding.root
        rootView.addView(recyclerView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MealListFragment", "onViewCreated")

        setupRecyclerView()
        setupLoadingAndErrorViews() // Initialize loading and error views
        observeViewModel()

        val category = arguments?.getString("category")
        val area = arguments?.getString("area")
        viewModel.setFilters(category, area)
    }

    private fun setupRecyclerView() {
        Log.d("MealListFragment", "setupRecyclerView")
        mealAdapter = MealAdapter(emptyList()) { meal ->
            Log.d("MealListFragment", "Meal clicked: ${meal.strMeal}")
            navigateToMealDetails(meal.idMeal)
        }
        binding.mealsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.mealsRecyclerView.adapter = mealAdapter
    }

    private fun setupLoadingAndErrorViews() {
        // Create ProgressBar
        loadingView = ProgressBar(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // Center the ProgressBar
                gravity = android.view.Gravity.CENTER
            }
            visibility = View.GONE // Initially hidden
        }
        rootView.addView(loadingView)

        // Create TextView for Error messages
        errorTextView = TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // Center the TextView
                gravity = android.view.Gravity.CENTER
            }
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            visibility = View.GONE // Initially hidden
            setTextColor(resources.getColor(R.color.error_color, requireActivity().theme)) // Set error color.  Create color if needed.
        }
        rootView.addView(errorTextView)
    }

    private fun observeViewModel() {
        Log.d("MealListFragment", "observeViewModel")
        viewModel.meals.observe(viewLifecycleOwner) { meals ->
            Log.d("MealListFragment", "meals.observe")
            mealAdapter.updateData(meals)
            binding.mealsRecyclerView.visibility = if (meals.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("MealListFragment", "loading.observe: $isLoading")
            loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.mealsRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE // Hide recycler view when loading
            errorTextView.visibility = View.GONE // Ensure error is hidden when loading starts

            // Change background color based on loading state
            rootView.setBackgroundColor(if (isLoading) resources.getColor(android.R.color.white, requireActivity().theme) else resources.getColor(android.R.color.black, requireActivity().theme))
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.d("MealListFragment", "error.observe: $error")
            if (error != null) {
                Log.e("MealListFragment", "Error: $error")
                errorTextView.text = error
                errorTextView.visibility = View.VISIBLE
                binding.mealsRecyclerView.visibility = View.GONE // Hide recycler view on error
                loadingView.visibility = View.GONE
                // Set background color to black on error
                rootView.setBackgroundColor(resources.getColor(android.R.color.black, requireActivity().theme))
            } else {
                errorTextView.visibility = View.GONE // Hide error message if no error
            }
        }
    }

    private fun navigateToMealDetails(mealId: String) {
        Log.d("MealListFragment", "navigateToMealDetails: $mealId")
        val bundle = Bundle().apply {
            putString("mealId", mealId)
        }
        findNavController().navigate(
            R.id.action_mealListFragment_to_mealDetailsFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MealListFragment", "onDestroyView")
        _binding = null
    }
}

