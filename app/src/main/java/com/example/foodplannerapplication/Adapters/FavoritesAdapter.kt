package com.example.foodplannerapplication.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.R

class FavoritesAdapter(
    private val favoriteMeals: List<FavoriteMeal>,
    private val onItemClick: (FavoriteMeal) -> Unit,
    private val onDeleteClick: (FavoriteMeal) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImageView: ImageView = itemView.findViewById(R.id.mealImageView)
        val mealNameTextView: TextView = itemView.findViewById(R.id.mealNameTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_meal, parent, false) // use new list item
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val meal = favoriteMeals[position]
        holder.mealNameTextView.text = meal.name

        Glide.with(holder.mealImageView.context)
            .load(meal.imageUrl)
            .into(holder.mealImageView)

        holder.itemView.setOnClickListener { onItemClick(meal) }
        holder.deleteButton.setOnClickListener { onDeleteClick(meal) }
    }

    override fun getItemCount(): Int = favoriteMeals.size
}