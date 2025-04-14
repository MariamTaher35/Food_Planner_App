package com.example.foodplannerapplication.fragments



import android.os.Bundle

import android.util.Log

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.foodplannerapplication.Adapters.CategoryAdapter

import com.example.foodplannerapplication.Models.Category

import com.example.foodplannerapplication.Models.CategoryResponse

import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.R

import com.example.foodplannerapplication.databinding.FragmentCategoriesBinding

import retrofit2.Call

import retrofit2.Callback

import retrofit2.Response



class CategoriesFragment : Fragment() {



    private var _binding: FragmentCategoriesBinding? = null

    private val binding get() = _binding!!

    private val apiService = TheMealDBService.create()



    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        fetchCategories()

    }



    private fun fetchCategories() {

        val call = apiService.getCategories()

        call.enqueue(object : Callback<CategoryResponse> {

            override fun onResponse(

                call: Call<CategoryResponse>,

                response: Response<CategoryResponse>

            ) {

                if (response.isSuccessful) {

                    val categories = response.body()?.categories ?: emptyList()

                    setupRecyclerView(categories)

                } else {

                    Log.e("CategoriesFragment", "API request failed: ${response.message()}")

                }

            }



            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {

                Log.e("CategoriesFragment", "API request failed: ${t.message}")

            }

        })

    }


    private fun setupRecyclerView(categories: List<Category>) {
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CategoryAdapter(categories) { category ->
                // Navigate to MealListFragment with the category name
                val bundle = Bundle().apply {
                    putString("category", category.strCategory)
                }
                findNavController().navigate(
                    R.id.action_categoriesFragment_to_mealListFragment,
                    bundle
                )
                Log.d("CategoriesFragment", "Category clicked: ${category.strCategory}")
            }
        }
    }



    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}