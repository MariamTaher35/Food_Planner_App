package com.example.foodplannerapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.ViewModels.MealDetailsViewModel  // Import the ViewModel
import com.example.foodplannerapplication.databinding.FragmentMealDetailsBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class MealDetailsFragment : Fragment() {

    private var _binding: FragmentMealDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mealDetailsViewModel: MealDetailsViewModel  // Use the ViewModel
    private var mealId: String? = null

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
    ): View {
        _binding = FragmentMealDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        mealDetailsViewModel = ViewModelProvider(this).get(MealDetailsViewModel::class.java)
        mealDetailsViewModel.setMealId(mealId)

        // Observe data from the ViewModel
        observeMealDetails()
        observeLoading()
        observeError()
        observeIsFavorite()

        // Setup UI based on authentication state
        if (!mealDetailsViewModel.authManager.isGuestMode()) {
            setupFavoriteIcon()
            binding.addToPlannedMealsButton.setOnClickListener {
                showPlanMealDialog()
            }
        } else {
            binding.addToPlannedMealsButton.isEnabled = false;
            binding.youtubePlayerView.visibility = View.GONE
        }

        // Check if the fragment is opened from the plan
        val isFromPlan = arguments?.getBoolean("isFromPlan", false) ?: false
        if (isFromPlan) {
            binding.addToPlannedMealsButton.visibility = View.GONE
        }
    }

    private fun observeMealDetails() {
        mealDetailsViewModel.meal.observe(viewLifecycleOwner) { meal ->
            meal?.let {
                updateUI(it)
            }
        }
    }

    private fun observeLoading() {
        mealDetailsViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun observeError() {
        mealDetailsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                Log.e("MealDetailsFragment", "Error: $it")
            }
        }
    }

    private fun observeIsFavorite() {
        mealDetailsViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            val favoriteIcon: ImageView? = binding.favoriteIconImageView
            favoriteIcon?.setImageResource(if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        }
    }


    private fun setupFavoriteIcon() {
        val favoriteIcon: ImageView? = binding.favoriteIconImageView
        favoriteIcon?.setOnClickListener {
            mealDetailsViewModel.addMealToFavorites()
        }
    }

    private fun updateUI(meal: Meal) {
        binding.mealNameTextView.text = meal.strMeal
        binding.mealCategoryTextView.text = "${meal.strCategory}"
        binding.mealAreaTextView.text = "${meal.strArea}"
        binding.instructionsDetailTextView.text = meal.strInstructions

        Glide.with(requireContext())
            .load(meal.strMealThumb)
            .into(binding.mealImageView)

        binding.ingredientsLayout.removeAllViews()
        meal.getIngredientsAndMeasures().forEach { (ingredient, measure) ->
            val ingredientView = TextView(context).apply {
                text = "${measure ?: ""} ${ingredient ?: ""}"
                setTextColor(Color.parseColor("#5E705B"))
                textSize = 16f
            }
            binding.ingredientsLayout.addView(ingredientView)
        }

        //show youtube
        if (!mealDetailsViewModel.authManager.isGuestMode()) {
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
                            Log.e("MealDetailsFragment", "Invalid Video ID")
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

    private fun showPlanMealDialog() {
        val planMealDialogFragment = PlanMealDialogFragment()
        val bundle = Bundle().apply {
            putString("mealName",  mealDetailsViewModel.meal.value?.strMeal)
            putString("mealImageUrl", mealDetailsViewModel.meal.value?.strMealThumb)
            putString("mealId", mealDetailsViewModel.meal.value?.idMeal)
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

