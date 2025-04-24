package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodplannerapplication.Adapters.FavoritesAdapter
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var appDatabase: AppDatabase
    private lateinit var authManager: AuthManager
    private lateinit var guestMessageTextView: TextView
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

        if (authManager.isGuestMode()) {
            recyclerView.visibility = View.GONE
            guestMessageTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            guestMessageTextView.visibility = View.GONE

            observeFavorites()
        }
    }

    private fun observeFavorites() {
        userId?.let { currentUserId ->
            lifecycleScope.launch {
                appDatabase.favoriteMealDao().getAll(currentUserId).collectLatest { favoriteMeals -> // Use userId
                    withContext(Dispatchers.Main) {
                        adapter = FavoritesAdapter(favoriteMeals, { meal ->  // Pass the meal object
                            navigateToMealDetails(meal.id)
                        }, { meal ->
                            deleteFavoriteMeal(meal)
                        })
                        recyclerView.adapter = adapter
                    }
                }
            }
        }
    }

    private fun navigateToMealDetails(mealId: String) {
        val bundle = Bundle().apply {
            putString("mealId", mealId)
        }
        findNavController().navigate(R.id.mealDetailsFragment, bundle)
    }

    private fun deleteFavoriteMeal(meal: FavoriteMeal) {
        userId?.let { currentUserId ->
            lifecycleScope.launch(Dispatchers.IO) {
                if (meal.userId == currentUserId) { // Ensure it belongs to the current user
                    appDatabase.favoriteMealDao().delete(meal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "${meal.name} removed from favorites", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
