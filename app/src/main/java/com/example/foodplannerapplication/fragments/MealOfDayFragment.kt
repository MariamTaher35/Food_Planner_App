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
import com.example.foodplannerapplication.databinding.FragmentMealOfDayBinding
import com.google.firebase.auth.FirebaseAuth
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealOfDayFragment : Fragment() {

    private var _binding: FragmentMealOfDayBinding? = null
    private val binding get() = _binding!!
    private val apiService = TheMealDBService.create()
    private lateinit var authManager: AuthManager
    private lateinit var appDatabase: AppDatabase
    private var meal: Meal? = null
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMealOfDayBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        appDatabase = AppDatabase.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        fetchRandomMeal()

        if (!authManager.isGuestMode()) {
            binding.addToFavoritesButton.setOnClickListener {
                meal?.let { addMealToFavorites(it) }
            }
            binding.addToPlannedMealsButton.setOnClickListener {
                meal?.let { showPlanMealDialog(it) }
            }
        } else {
            binding.addToFavoritesButton.isEnabled = false
            binding.addToPlannedMealsButton.isEnabled = false
            binding.youtubePlayerView.visibility = View.GONE
        }
    }

    private fun fetchRandomMeal() {
        val call = apiService.getRandomMeal()
        call.enqueue(object : Callback<MealsResponse> {
            override fun onResponse(
                call: Call<MealsResponse>,
                response: Response<MealsResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    meal = response.body()?.meals?.firstOrNull()
                    meal?.let { updateUI(it) } ?: run {
                        Log.e("MealOfDayFragment", "No meal found in response")
                        Toast.makeText(context, "Failed to load meal of the day", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MealOfDayFragment", "API request failed: ${response.message()}")
                    Toast.makeText(context, "Failed to load meal of the day", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("MealOfDayFragment", "API request failed: ${t.message}")
                Toast.makeText(context, "Failed to load meal of the day", Toast.LENGTH_SHORT).show()
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
            if (!meal.strYoutube.isNullOrEmpty()) {
                binding.youtubePlayerView.visibility = View.VISIBLE
                lifecycle.addObserver(binding.youtubePlayerView)
                binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        val videoId = meal.strYoutube.substringAfterLast("v=")
                        if (videoId.isNotEmpty()) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        } else {
                            Log.e("MealOfDayFragment", "Invalid Video ID")
                        }
                    }
                })
            } else {
                binding.youtubePlayerView.visibility = View.GONE
            }
        } else {
            binding.youtubePlayerView.visibility = View.GONE
        }
    }

    private fun addMealToFavorites(meal: Meal) {
        Log.d("MealOfDay", "addMealToFavorites called")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = userId
                if (currentUserId != null) {
                    val favoriteMeal = FavoriteMeal(
                        id = meal.idMeal,
                        name = meal.strMeal,
                        imageUrl = meal.strMealThumb,
                        originCountry = null,
                        ingredients = null,
                        steps = null,
                        videoUrl = null,
                        guestMode = authManager.isGuestMode(),
                        userId = currentUserId
                    )
                    appDatabase.favoriteMealDao().insert(favoriteMeal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MealOfDay", "Failed to add to Favorites: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to add to Favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPlanMealDialog(meal: Meal) {
        val planMealDialogFragment = PlanMealDialogFragment()
        val bundle = Bundle().apply {
            putString("mealName", meal.strMeal)
            putString("mealImageUrl", meal.strMealThumb)
            putString("mealId", meal.idMeal)
        }
        planMealDialogFragment.arguments = bundle
        planMealDialogFragment.show(childFragmentManager, "PlanMealDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubePlayerView.release()
        _binding = null
    }
}