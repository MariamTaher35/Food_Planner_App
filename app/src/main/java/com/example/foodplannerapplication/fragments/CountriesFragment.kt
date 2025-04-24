package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodplannerapplication.Adapters.AreaAdapter
import com.example.foodplannerapplication.Models.Area
import com.example.foodplannerapplication.Models.AreaResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CountriesFragment : Fragment() {

    private val apiService = TheMealDBService.create()
    private var countriesList: List<Area> = emptyList()
    private lateinit var countriesRecyclerView: RecyclerView
    private lateinit var areaAdapter: AreaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_countries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countriesRecyclerView = view.findViewById(R.id.countriesRecyclerView)
        setupRecyclerView()
        fetchCountries()
    }

    private fun setupRecyclerView() {
        countriesRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = 4 // 4dp top space between items
            }
        })

        countriesRecyclerView.layoutManager = LinearLayoutManager(context)
        areaAdapter = AreaAdapter(emptyList()) { country ->
            val bundle = Bundle().apply {
                putString("area", country.strArea)
            }
            findNavController().navigate(
                R.id.action_countriesFragment_to_mealListFragment,
                bundle
            )
        }
        countriesRecyclerView.adapter = areaAdapter
    }

    private fun fetchCountries() {
        apiService.getAreas().enqueue(object : Callback<AreaResponse> {
            override fun onResponse(call: Call<AreaResponse>, response: Response<AreaResponse>) {
                if (response.isSuccessful) {
                    val areaResponse = response.body()
                    areaResponse?.meals?.let {
                        countriesList = it
                        areaAdapter.updateData(countriesList)
                    }
                } else {
                    Log.e("CountriesFragment", "Failed to fetch countries: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Log.e("CountriesFragment", "Error fetching countries: ${t.message}")
            }
        })
    }
}
