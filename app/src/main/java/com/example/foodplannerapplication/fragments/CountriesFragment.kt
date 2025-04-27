package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodplannerapplication.Adapters.AreaAdapter
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.CountriesViewModel
import com.example.foodplannerapplication.databinding.FragmentCountriesBinding // Import the binding

class CountriesFragment : Fragment() {

    private var _binding: FragmentCountriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var countriesRecyclerView: RecyclerView
    private lateinit var areaAdapter: AreaAdapter
    private lateinit var viewModel: CountriesViewModel
    private lateinit var errorTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCountriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using view binding
        countriesRecyclerView = binding.countriesRecyclerView
        errorTextView = binding.errorTextView // Access from binding
        progressBar = binding.progressBar    // Access from binding

        setupRecyclerView()
        initializeViewModel()
        observeViewModel()
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

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this).get(CountriesViewModel::class.java)
    }

    private fun observeViewModel() {
        viewModel.countries.observe(viewLifecycleOwner) { countries ->
            areaAdapter.updateData(countries)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            errorTextView.text = error
            errorTextView.visibility = if (error.isNotEmpty()) View.VISIBLE else View.GONE
            Log.e("CountriesFragment", error)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

