package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.findNavController
import com.example.foodplannerapplication.Adapters.MealAdapter
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.databinding.FragmentSearchBinding
import com.example.foodplannerapplication.viewmodels.SearchViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

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
                viewModel.searchByName(searchTerm)
                viewModel.searchByCategory(searchTerm)
                viewModel.searchByArea(searchTerm)
            }
        }

        viewModel.mealsLiveData.observe(viewLifecycleOwner) { meals ->
            if (meals.isNotEmpty()) setupRecyclerView(meals)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            Log.e("SearchFragment", error)
        }
    }

    private fun setupRecyclerView(meals: List<Meal>) {
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRecyclerView.adapter = MealAdapter(meals, true) { meal ->
            val bundle = Bundle().apply { putString("mealId", meal.idMeal) }
            view?.findNavController()?.navigate(
                R.id.action_searchFragment_to_mealDetailsFragment,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
