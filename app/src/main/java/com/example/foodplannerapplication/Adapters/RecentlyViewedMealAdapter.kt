package com.example.foodplannerapplication.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.databinding.ItemRecentlyViewedMealBinding // Import for new binding

class RecentlyViewedMealAdapter(
    private var meals: List<Meal>,
    private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<RecentlyViewedMealAdapter.RecentlyViewedMealViewHolder>() {

    inner class RecentlyViewedMealViewHolder(private val binding: ItemRecentlyViewedMealBinding) :  // Use the new binding
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: Meal) {
            binding.mealNameTextView.text = meal.strMeal
            Glide.with(binding.mealImageView.context)
                .load(meal.strMealThumb)
                .into(binding.mealImageView)
            binding.root.setOnClickListener {
                onItemClick(meal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentlyViewedMealViewHolder {
        val binding = ItemRecentlyViewedMealBinding.inflate( // Use the new binding
            LayoutInflater.from(parent.context), parent, false
        )
        return RecentlyViewedMealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentlyViewedMealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    fun updateData(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}