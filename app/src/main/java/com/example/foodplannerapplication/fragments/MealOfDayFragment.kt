package com.example.foodplannerapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.MealOfDayViewModel // Import ViewModel
import com.example.foodplannerapplication.databinding.FragmentMealOfDayBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class MealOfDayFragment : Fragment() {

    private var _binding: FragmentMealOfDayBinding? = null
    private val binding get() = _binding!!
    private lateinit var mealOfDayViewModel: MealOfDayViewModel // Use ViewModel
    private var meal: Meal? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMealOfDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        mealOfDayViewModel = ViewModelProvider(this).get(MealOfDayViewModel::class.java)

        // Observe LiveData from ViewModel
        observeMeal()
        observeLoading()
        observeError()

        // Setup click listeners
        if (!mealOfDayViewModel.authManager.isGuestMode()) {
            binding.addToFavoritesButton.setOnClickListener {
                mealOfDayViewModel.addMealToFavorites()
            }
            binding.addToPlannedMealsButton.setOnClickListener {
                mealOfDayViewModel.showPlanMealDialog()
                meal?.let { showPlanMealDialog(it) }
            }
        } else {
            binding.addToFavoritesButton.isEnabled = false
            binding.addToPlannedMealsButton.isEnabled = false
            binding.youtubePlayerView.visibility = View.GONE
        }
    }

    private fun observeMeal() {
        mealOfDayViewModel.meal.observe(viewLifecycleOwner, Observer { fetchedMeal ->
            fetchedMeal?.let {
                updateUI(it)
                meal = it
            }
        })
    }

    private fun observeLoading() {
        mealOfDayViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun observeError() {
        mealOfDayViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                Log.e("MealOfDayFragment", "Error: $it")
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

        if (!mealOfDayViewModel.authManager.isGuestMode()) {
            if (!meal.strYoutube.isNullOrEmpty()) {
                binding.youtubePlayerView.visibility = View.VISIBLE
                lifecycle.addObserver(binding.youtubePlayerView)
                binding.youtubePlayerView.addYouTubePlayerListener(object :
                    AbstractYouTubePlayerListener() {
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
