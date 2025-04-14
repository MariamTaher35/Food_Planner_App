package com.example.foodplannerapplication.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_meals", primaryKeys = ["id", "userId"])
data class FavoriteMeal(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val originCountry: String?,
    val ingredients: String?,
    val steps: String?,
    val videoUrl: String?,
    val guestMode: Boolean = false,
    val userId: String
)