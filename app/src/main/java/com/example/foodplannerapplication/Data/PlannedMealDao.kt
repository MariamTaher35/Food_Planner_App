package com.example.foodplannerapplication.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedMealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: PlannedMeal)

    @Query("SELECT * FROM planned_meals WHERE userId = :userId")
    fun getAll(userId: String): Flow<List<PlannedMeal>>

    @Delete
    suspend fun delete(meal: PlannedMeal)

    @Query("DELETE FROM planned_meals WHERE id = :mealId AND userId = :userId")
    suspend fun deleteById(mealId: String, userId: String)

    @Query("SELECT * FROM planned_meals WHERE id = :mealId AND userId = :userId LIMIT 1")
    suspend fun getMealById(mealId: String, userId: String): PlannedMeal?

    @Query("SELECT * FROM planned_meals WHERE dayOfWeek = :dayOfWeek AND mealTime = :mealTime AND userId = :userId")
    suspend fun getMealsByDayAndTime(dayOfWeek: String, mealTime: String, userId: String): List<PlannedMeal>
}