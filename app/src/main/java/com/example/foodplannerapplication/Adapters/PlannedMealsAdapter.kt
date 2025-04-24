package com.example.foodplannerapplication.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.databinding.ItemPlannedMealBinding

class PlannedMealsAdapter(
    private val onDeleteClick: (PlannedMeal) -> Unit
) : ListAdapter<PlannedMeal, PlannedMealsAdapter.PlannedMealViewHolder>(PlannedMealDiffCallback()) {

    var onMealClick: ((PlannedMeal) -> Unit)? = null

    inner class PlannedMealViewHolder(val binding: ItemPlannedMealBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meal: PlannedMeal) {
            binding.mealNameTextView.text = meal.mealName
            binding.dayOfWeekTextView.text = meal.dayOfWeek
            binding.mealTimeTextView.text = meal.mealTime
            Glide.with(itemView.context).load(meal.mealImageUrl).into(binding.mealImageView)
            binding.deleteButton.setOnClickListener { onDeleteClick(meal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlannedMealViewHolder {
        val binding = ItemPlannedMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlannedMealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlannedMealViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            onMealClick?.invoke(currentItem)
        }
    }

    override fun onCurrentListChanged(previousList: MutableList<PlannedMeal>, currentList: MutableList<PlannedMeal>) {
        super.onCurrentListChanged(previousList, currentList)
    }

    class PlannedMealDiffCallback : DiffUtil.ItemCallback<PlannedMeal>() {
        override fun areItemsTheSame(oldItem: PlannedMeal, newItem: PlannedMeal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlannedMeal, newItem: PlannedMeal): Boolean {
            return oldItem == newItem
        }
    }
}
