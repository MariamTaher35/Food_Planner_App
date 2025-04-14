package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.findNavController
import com.example.foodplannerapplication.Adapters.MealAdapter
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val apiService = TheMealDBService.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchButton.setOnClickListener {
            val searchTerm = binding.searchEditText.text.toString().trim()
            if (searchTerm.isNotEmpty()) {
                performSearch(searchTerm)
            }
        }
    }

    private fun performSearch(searchTerm: String) {
        val call = apiService.searchMeals(searchTerm)
        call.enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                if (response.isSuccessful) {
                    val meals = response.body()?.meals ?: emptyList()
                    setupRecyclerView(meals)
                } else {
                    Log.e("SearchFragment", "API request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                Log.e("SearchFragment", "API request failed: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(meals: List<Meal>) {
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRecyclerView.adapter = MealAdapter(meals, true) { meal -> // Passing true here
            // Handle meal click here (e.g., navigate to meal details)
            Log.d("SearchFragment", "Meal clicked: ${meal.strMeal}")
            val bundle = Bundle().apply {
                putString("mealId", meal.idMeal)
            }
            view?.findNavController()?.navigate(
                R.id.action_searchFragment_to_mealDetailsFragment,  // Use the correct action ID from your nav_graph!
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}