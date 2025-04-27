package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Adapters.RecentlyViewedMealAdapter
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.databinding.FragmentHomeBinding
import com.example.foodplannerapplication.ViewModels.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var recentlyViewedMealsAdapter: RecentlyViewedMealAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Set up categories RecyclerView
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = viewModel.categoryAdapter
        }

        // Set up recently viewed meals RecyclerView
        recentlyViewedMealsAdapter = RecentlyViewedMealAdapter(emptyList()) { meal ->
            navigateToMealDetails(meal.idMeal)
        }
        binding.recentlyViewedMealsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentlyViewedMealsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.mealOfDay.observe(viewLifecycleOwner) { meal ->
            meal?.let { updateMealOfDayUI(it) }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            viewModel.categoryAdapter.updateData(categories)
        }

        viewModel.recentlyViewedMeals.observe(viewLifecycleOwner) { meals ->
            recentlyViewedMealsAdapter.updateData(meals)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("HomeFragment", "Error: $it")
                // Handle error (e.g., show a Toast)
            }
        }

        // Observe the navigation event from the ViewModel
        viewModel.navigateToMealList.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { (filterType, filterValue) ->
                navigateToMealList(filterType, filterValue)
            }
        }
    }

    private fun updateMealOfDayUI(meal: Meal) {
        binding.mealOfDayNameTextView.text = meal.strMeal
        Glide.with(requireContext())
            .load(meal.strMealThumb)
            .into(binding.mealOfDayImageView)

        binding.mealOfDayCardView.setOnClickListener {
            navigateToMealDetails(meal.idMeal)
        }
    }

    private fun navigateToMealDetails(mealId: String) {
        val bundle = Bundle().apply {
            putString("mealId", mealId)
        }
        findNavController().navigate(R.id.action_homeFragment_to_mealDetailsFragment, bundle)
    }

    private fun navigateToMealList(filterType: String, filterValue: String) {
        val bundle = Bundle().apply {
            putString(filterType, filterValue)
        }
        findNavController().navigate(R.id.action_homeFragment_to_mealListFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}