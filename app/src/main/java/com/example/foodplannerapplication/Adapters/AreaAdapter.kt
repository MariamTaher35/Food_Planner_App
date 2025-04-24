package com.example.foodplannerapplication.Adapters

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodplannerapplication.Models.Area

class AreaAdapter(
    private var areas: List<Area>,
    private val onItemClick: (Area) -> Unit
) : RecyclerView.Adapter<AreaAdapter.AreaViewHolder>() {

    inner class AreaViewHolder(val container: LinearLayout) : RecyclerView.ViewHolder(container) {
        val textView: TextView = container.getChildAt(0) as TextView

        fun bind(area: Area) {
            textView.text = area.strArea
            textView.setTextColor(Color.parseColor("#5e705b"))

            container.setOnClickListener {
                onItemClick(area)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val context = parent.context

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(16.dp(context), 16.dp(context), 16.dp(context), 16.dp(context))
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8.dp(context), 8.dp(context), 8.dp(context), 8.dp(context))
            }
        }

        val textView = TextView(context).apply {
            textSize = 16f
            setTextColor(Color.parseColor("#5e705b"))
        }

        container.addView(textView)

        return AreaViewHolder(container)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        holder.bind(areas[position])
    }

    override fun getItemCount(): Int = areas.size

    fun updateData(newAreas: List<Area>) {
        areas = newAreas
        notifyDataSetChanged()
    }

    private fun Int.dp(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}



