package com.example.foodplannerapplication.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Adapters.CategoryAdapter
import com.example.foodplannerapplication.Adapters.RecentlyViewedMealAdapter
import com.example.foodplannerapplication.Models.Category
import com.example.foodplannerapplication.Models.CategoryResponse
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val apiService = TheMealDBService.create()

    private var mealOfDay: Meal? = null
    private var categoriesList: List<Category> = emptyList()
    private var recentlyViewedMealsList: MutableList<Meal> = mutableListOf()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recentlyViewedMealsAdapter: RecentlyViewedMealAdapter
    private val sharedPrefsKey = "recently_viewed_meals"
    private val maxRecentlyViewed = 8

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        setupRecyclerViews()
        fetchMealOfDay()
        fetchCategories()
        loadRecentlyViewedMeals()
    }

    private fun setupRecyclerViews() {
        // Initialize Category RecyclerView and Adapter
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            navigateToMealList("category", category.strCategory)
        }
        binding.categoriesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        // Initialize Recently Viewed RecyclerView and Adapter
        recentlyViewedMealsAdapter =
            RecentlyViewedMealAdapter(recentlyViewedMealsList) { meal ->
                navigateToMealDetails(meal.idMeal)
            }
        binding.recentlyViewedMealsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentlyViewedMealsAdapter
        }
    }

    private fun fetchMealOfDay() {
        apiService.getRandomMeal().enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                if (response.isSuccessful) {
                    mealOfDay = response.body()?.meals?.firstOrNull()
                    mealOfDay?.let {
                        updateMealOfDayUI(it)
                        saveToRecentlyViewed(it)
                    } ?: run {
                        Log.w("HomeFragment", "Meal of the day is null")
                        checkLoadingState()
                    }
                } else {
                    Log.e("HomeFragment", "Failed to fetch meal of the day: ${response.message()}")
                    checkLoadingState()
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching meal of the day: ${t.message}")
                checkLoadingState()
            }
        })
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

    private fun fetchCategories() {
        apiService.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                if (response.isSuccessful) {
                    categoriesList = response.body()?.categories ?: emptyList()
                    categoryAdapter.updateData(categoriesList)
                    checkLoadingState()
                } else {
                    Log.e("HomeFragment", "Failed to fetch categories: ${response.message()}")
                    checkLoadingState()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching categories: ${t.message}")
                checkLoadingState()
            }
        })
    }

    private fun navigateToMealDetails(mealId: String) {
        val bundle = Bundle().apply {
            putString("mealId", mealId)
        }
        findNavController().navigate(
            R.id.action_homeFragment_to_mealDetailsFragment,
            bundle
        )
    }

    private fun navigateToMealList(filterType: String, filterValue: String) {
        val bundle = Bundle().apply {
            putString(filterType, filterValue)
        }
        findNavController().navigate(
            R.id.action_homeFragment_to_mealListFragment,
            bundle
        )
    }

    private fun checkLoadingState() {
        if (mealOfDay != null && categoryAdapter.itemCount > 0) {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadRecentlyViewedMeals() {
        val sharedPrefs = requireContext().getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPrefs.getString("recently_viewed", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Meal>>() {}.type
                val loadedList: List<Meal> =
                    gson.fromJson(json, type) ?: emptyList() // Specify the type here
                recentlyViewedMealsList.clear()
                recentlyViewedMealsList.addAll(loadedList)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error parsing JSON: ${e.message}")
                recentlyViewedMealsList.clear()
            }
        }
        recentlyViewedMealsAdapter.updateData(recentlyViewedMealsList)
    }

    private fun saveToRecentlyViewed(meal: Meal) {
        val sharedPrefs = requireContext().getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val gson = Gson()

        // Load existing list
        val existingJson = sharedPrefs.getString("recently_viewed", null)
        val type: Type = object : TypeToken<List<Meal>>() {}.type
        val existingList: MutableList<Meal> =
            if (existingJson != null) gson.fromJson(existingJson, type) ?: mutableListOf() else mutableListOf()

        // Check if the meal is already in the list
        val mealIndex = existingList.indexOfFirst { it.idMeal == meal.idMeal }
        if (mealIndex != -1) {
            existingList.removeAt(mealIndex)
        }
        // Add the new meal to the front
        existingList.add(0, meal)
        // Keep only the last 'maxRecentlyViewed' meals
        if (existingList.size > maxRecentlyViewed) {
            existingList.subList(maxRecentlyViewed, existingList.size).clear()
        }

        val json = gson.toJson(existingList)
        editor.putString("recently_viewed", json)
        editor.apply()
        recentlyViewedMealsList.clear()
        recentlyViewedMealsList.addAll(existingList)
        recentlyViewedMealsAdapter.updateData(existingList)
    }

    // Adapter update functions
    fun CategoryAdapter.updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}

