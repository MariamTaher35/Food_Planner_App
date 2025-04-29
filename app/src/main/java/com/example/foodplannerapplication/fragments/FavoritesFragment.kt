package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodplannerapplication.Adapters.FavoritesAdapter
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.FavoritesViewModel
import com.example.foodplannerapplication.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var appDatabase: AppDatabase
    private lateinit var authManager: AuthManager
    private lateinit var guestMessageTextView: TextView
    private lateinit var viewModel: FavoritesViewModel

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
        guestMessageTextView = view.findViewById(R.id.guest_mode_message)
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        appDatabase = AppDatabase.getInstance(requireContext())

        val viewModelFactory = FavoritesViewModelFactory(appDatabase, userId)
        viewModel = ViewModelProvider(this, viewModelFactory)[FavoritesViewModel::class.java]

        if (authManager.isGuestMode()) {
            recyclerView.visibility = View.GONE
            guestMessageTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            guestMessageTextView.visibility = View.GONE
            setupObservers()
        }
    }

    private fun setupObservers() {
        viewModel.favoriteMeals.observe(viewLifecycleOwner) { favoriteMeals ->
            adapter = FavoritesAdapter(favoriteMeals.toMutableList(), { meal ->
                navigateToMealDetails(meal.id)
            }, { meal ->
                showDeleteConfirmationDialog(meal)
            })
            recyclerView.adapter = adapter
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            // Optional: Add progress indicator handling
        }
    }

    private fun navigateToMealDetails(mealId: String) {
        val bundle = Bundle().apply {
            putString("mealId", mealId)
        }
        findNavController().navigate(R.id.mealDetailsFragment, bundle)
    }

    private fun showDeleteConfirmationDialog(meal: FavoriteMeal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Favorite")
            .setMessage("Are you sure you want to delete \"${meal.name}\" from your favorites?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteFavoriteMeal(meal)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}

class FavoritesViewModelFactory(
    private val appDatabase: AppDatabase,
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(appDatabase, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
