package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodplannerapplication.Adapters.CategoryAdapter
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.CategoriesViewModel
import com.example.foodplannerapplication.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(CategoriesViewModel::class.java)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList()) { category ->  // Initialize with empty list
            // Navigate to MealListFragment with the category name
            val bundle = Bundle().apply {
                putString("category", category.strCategory)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_mealListFragment, // Changed action ID
                bundle
            )
            Log.d("CategoriesFragment", "Category clicked: ${category.strCategory}")
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }
    }

    private fun setupObservers() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.updateData(categories)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show or hide loading indicator (e.g., progress bar)
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE // Make sure you have a ProgressBar in your layout
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            // Handle error (e.g., show a Toast, display a message in a TextView)
            Log.e("CategoriesFragment", error)
            // Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            binding.errorTextView.text = error //display error message
            binding.errorTextView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
