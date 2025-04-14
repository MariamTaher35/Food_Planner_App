package com.example.foodplannerapplication.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_meals", primaryKeys = ["id", "userId"])
data class PlannedMeal(
    val id: String,
    val dayOfWeek: String,
    val mealTime: String,
    val guestMode: Boolean = false,
    val mealName: String?,
    val mealImageUrl: String?,
    val userId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlannedMeal

        if (id != other.id) return false
        if (dayOfWeek != other.dayOfWeek) return false
        if (mealTime != other.mealTime) return false
        if (userId != other.userId) return false // Include userId in equals

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + dayOfWeek.hashCode()
        result = 31 * result + mealTime.hashCode()
        result = 31 * result + userId.hashCode() // Include userId in hashCode
        return result
    }
}