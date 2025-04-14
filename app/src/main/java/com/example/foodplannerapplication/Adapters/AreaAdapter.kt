package com.example.foodplannerapplication.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodplannerapplication.Models.Area
import com.example.foodplannerapplication.databinding.ItemAreaBinding // Corrected import

class AreaAdapter(
    private val areas: List<Area>,
    private val onItemClick: (Area) -> Unit
) : RecyclerView.Adapter<AreaAdapter.AreaViewHolder>() {

    inner class AreaViewHolder(private val binding: ItemAreaBinding) : // Corrected binding type
        RecyclerView.ViewHolder(binding.root) {

        fun bind(area: Area) {
            binding.areaNameTextView.text = area.strArea // Corrected view id

            binding.root.setOnClickListener {
                onItemClick(area)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val binding = ItemAreaBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Corrected binding inflate
        return AreaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        holder.bind(areas[position])
    }

    override fun getItemCount(): Int = areas.size
}