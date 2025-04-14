package com.example.foodplannerapplication.fragments



import android.os.Bundle

import android.util.Log

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.foodplannerapplication.Adapters.AreaAdapter

import com.example.foodplannerapplication.Models.Area

import com.example.foodplannerapplication.Models.AreaResponse

import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.R

import com.example.foodplannerapplication.databinding.FragmentCountriesBinding

import retrofit2.Call

import retrofit2.Callback

import retrofit2.Response



class CountriesFragment : Fragment() {



    private var _binding: FragmentCountriesBinding? = null

    private val binding get() = _binding!!

    private val apiService = TheMealDBService.create()



    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentCountriesBinding.inflate(inflater, container, false)

        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        fetchCountries()

    }



    private fun fetchCountries() {

        val call = apiService.getAreas()

        call.enqueue(object : Callback<AreaResponse> {

            override fun onResponse(

                call: Call<AreaResponse>,

                response: Response<AreaResponse>

            ) {

                if (response.isSuccessful) {

                    val areas = response.body()?.meals ?: emptyList()

                    setupRecyclerView(areas)

                } else {

                    Log.e("CountriesFragment", "API request failed: ${response.message()}")

                }

            }



            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {

                Log.e("CountriesFragment", "API request failed: ${t.message}")

            }

        })

    }


    private fun setupRecyclerView(areas: List<Area>) {
        binding.countriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AreaAdapter(areas) { area ->
                // Navigate to MealListFragment with the area name
                val bundle = Bundle().apply {
                    putString("area", area.strArea)
                }
                findNavController().navigate(
                    R.id.action_countriesFragment_to_mealListFragment,
                    bundle
                )
                Log.d("CountriesFragment", "Area clicked: ${area.strArea}")
            }
        }
    }



    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}