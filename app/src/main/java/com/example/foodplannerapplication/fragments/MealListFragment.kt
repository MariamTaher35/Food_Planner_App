package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodplannerapplication.Adapters.MealAdapter
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.databinding.FragmentMealListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealListFragment : Fragment() {

    private var _binding: FragmentMealListBinding? = null
    private val binding get() = _binding!!
    private val apiService = TheMealDBService.create()
    private var category: String? = null
    private var area: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString("category")
            area = it.getString("area")
        }
        Log.d("MealListFragment", "Category: $category, Area: $area")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMealListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (category != null) {
            fetchMealsByCategory(category!!)
        } else if (area != null) {
            fetchMealsByArea(area!!)
        }
    }

    private fun fetchMealsByCategory(category: String) {
        val call = apiService.getMealsByCategory(category)
        call.enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                if (response.isSuccessful) {
                    val meals = response.body()?.meals ?: emptyList()
                    setupRecyclerView(meals)
                } else {
                    Log.e("MealListFragment", "API request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                Log.e("MealListFragment", "API request failed: ${t.message}")
            }
        })
    }

    private fun fetchMealsByArea(area: String) {
        val call = apiService.getMealsByArea(area)
        call.enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                if (response.isSuccessful) {
                    val meals = response.body()?.meals ?: emptyList()
                    setupRecyclerView(meals)
                } else {
                    Log.e("MealListFragment", "API request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                Log.e("MealListFragment", "API request failed: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(meals: List<Meal>) {
        binding.mealsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.mealsRecyclerView.adapter = MealAdapter(meals) { meal ->
            // Handle meal click here (e.g., navigate to meal details)
            Log.d("MealListFragment", "Meal clicked: ${meal.strMeal}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}