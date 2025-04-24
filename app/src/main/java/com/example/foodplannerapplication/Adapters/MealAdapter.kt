package com.example.foodplannerapplication.Adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.databinding.ItemMealBinding
import android.graphics.Color // Import Color

class MealAdapter(
    var meals: List<Meal>,
    private val fromSearch: Boolean = false, // Add this parameter
    private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.binding.mealNameTextView.text = meal.strMeal
        holder.binding.mealNameTextView.setTextColor(Color.parseColor("#5e705b")) // Set the text color here
        Glide.with(holder.itemView.context)
            .load(meal.strMealThumb)
            .into(holder.binding.mealImageView)

        holder.itemView.setOnClickListener {
            Log.d("MealAdapter", "Item clicked: ${meal.idMeal}")
            val bundle = Bundle().apply {
                putString("mealId", meal.idMeal)
            }
            try {
                val actionId = if (fromSearch) {
                    R.id.action_searchFragment_to_mealDetailsFragment // Search Fragment Action
                } else {
                    R.id.action_mealListFragment_to_mealDetailsFragment // Meal List Fragment Action
                }
                holder.itemView.findNavController().navigate(
                    actionId,
                    bundle
                )
                onItemClick(meal)
            } catch (e: IllegalArgumentException) {
                Log.e("MealAdapter", "Navigation failed: ${e.message}")
                e.printStackTrace()
            }
            Log.d("MealAdapter", "Navigation attempt completed")
        }
    }

    override fun getItemCount(): Int = meals.size

    fun updateData(newMeals: List<Meal>) {
        meals = newMeals; //re-assign
        notifyDataSetChanged()
    }
}
