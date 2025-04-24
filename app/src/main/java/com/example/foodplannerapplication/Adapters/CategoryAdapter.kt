package com.example.foodplannerapplication.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.Models.Category
import com.example.foodplannerapplication.databinding.ItemCategoryCircularBinding // Corrected import

class CategoryAdapter(
    var categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemCategoryCircularBinding) : // Corrected binding type
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.categoryNameTextView.text = category.strCategory
            Glide.with(binding.categoryImageView.context)
                .load(category.strCategoryThumb)
                .into(binding.categoryImageView)

            binding.root.setOnClickListener {
                onItemClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryCircularBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Corrected binding inflate
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories // Simply re-assign the list
        notifyDataSetChanged()
    }
}