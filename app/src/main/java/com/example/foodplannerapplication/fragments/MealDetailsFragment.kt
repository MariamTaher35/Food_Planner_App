package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.MealsResponse
import com.example.foodplannerapplication.Models.TheMealDBService
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.FragmentMealDetailsBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealDetailsFragment : Fragment() {

    private var _binding: FragmentMealDetailsBinding? = null
    private val binding get() = _binding!!
    private val apiService = TheMealDBService.create()
    private var mealId: String? = null
    private lateinit var authManager: AuthManager
    private lateinit var appDatabase: AppDatabase
    private var meal: Meal? = null
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mealId = it.getString("mealId")
        }
        Log.d("MealDetailsFragment", "Meal ID: $mealId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMealDetailsBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        appDatabase = AppDatabase.getInstance(requireContext())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MealDetailsFragment", "onViewCreated") // Log
        mealId?.let { fetchMealDetails(it) }

        if (!authManager.isGuestMode()) {
            binding.addToFavoritesButton.setOnClickListener {
                addMealToFavorites()
            }

            binding.addToPlannedMealsButton.setOnClickListener {
                showPlanMealDialog()
            }
        } else {
            binding.addToFavoritesButton.isEnabled = false
            binding.addToPlannedMealsButton.isEnabled = false
            binding.youtubePlayerView.visibility = View.GONE
        }
    }

    private fun fetchMealDetails(mealId: String) {
        binding.progressBar.visibility = View.VISIBLE
        val call = apiService.getMealDetails(mealId)
        call.enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    meal = response.body()?.meals?.firstOrNull()
                    Log.d("MealDetailsFragment", "Fetched meal: $meal")
                    meal?.let { updateUI(it) } ?: run {
                        Log.e("MealDetailsFragment", "No meal found in response")
                        Toast.makeText(context, "No meal details found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MealDetailsFragment", "API request failed: ${response.message()}")
                    Toast.makeText(context, "Failed to fetch meal details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("MealDetailsFragment", "API request failed: ${t.message}")
                Toast.makeText(context, "Failed to fetch meal details: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(meal: Meal) {
        binding.mealNameTextView.text = meal.strMeal
        binding.mealCategoryTextView.text = "Category: ${meal.strCategory}"
        binding.mealAreaTextView.text = "Area: ${meal.strArea}"
        binding.instructionsDetailTextView.text = meal.strInstructions

        Glide.with(requireContext())
            .load(meal.strMealThumb)
            .into(binding.mealImageView)

        binding.ingredientsLayout.removeAllViews()
        meal.getIngredientsAndMeasures().forEach { (ingredient, measure) ->
            val ingredientView = TextView(context)
            ingredientView.text = "${measure ?: ""} ${ingredient ?: ""}"
            binding.ingredientsLayout.addView(ingredientView)
        }

        if (!authManager.isGuestMode()) {
            lifecycle.addObserver(binding.youtubePlayerView)
            binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    val videoId = meal.strYoutube.substringAfterLast("v=")
                    if (videoId.isNotEmpty()) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    } else {
                        Log.e("MealDetailsFragment", "Invalid Video ID")
                    }
                }
            })
        }
    }

    private fun addMealToFavorites() {
        Log.d("Favorites", "addMealToFavorites called")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                meal?.let { meal ->
                    Log.d("Favorites", "Meal is not null")
                    val currentUserId = userId // Get the userId here
                    if (currentUserId != null) {
                        Log.d("Favorites", "userId is not null: $currentUserId")
                        val favoriteMeal = FavoriteMeal(
                            id = meal.idMeal,
                            name = meal.strMeal,
                            imageUrl = meal.strMealThumb,
                            originCountry = null,
                            ingredients = null,
                            steps = null,
                            videoUrl = null,
                            guestMode = authManager.isGuestMode(),
                            userId = currentUserId // Set the userId here!
                        )
                        appDatabase.favoriteMealDao().insert(favoriteMeal)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("Favorites", "userId is null")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: withContext(Dispatchers.Main) {
                    Log.e("Favorites", "Meal data is incomplete")
                    Toast.makeText(context, "Meal data is incomplete", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Favorites", "Failed to add to Favorites: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to add to Favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPlanMealDialog() {
        meal?.let {
            val planMealDialogFragment = PlanMealDialogFragment()
            val bundle = Bundle().apply {
                putString("mealName", it.strMeal)
                putString("mealImageUrl", it.strMealThumb)
                putString("mealId", it.idMeal)
            }
            planMealDialogFragment.arguments = bundle
            planMealDialogFragment.show(childFragmentManager, "PlanMealDialog")
        } ?: run {
            Toast.makeText(context, "Meal data not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubePlayerView.release()
        _binding = null
    }
}