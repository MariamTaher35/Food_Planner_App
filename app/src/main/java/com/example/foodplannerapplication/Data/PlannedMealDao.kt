package com.example.foodplannerapplication.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedMealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: PlannedMeal)

    @Query("SELECT * FROM planned_meals WHERE userId = :userId") // Filter by userId
    fun getAll(userId: String): Flow<List<PlannedMeal>>

    @Delete
    suspend fun delete(meal: PlannedMeal)

    @Query("SELECT * FROM planned_meals WHERE id = :id AND userId = :userId") // Filter by userId
    suspend fun getById(id: String, userId: String): PlannedMeal?

    @Update
    suspend fun update(meal: PlannedMeal)

    @Query("DELETE FROM planned_meals WHERE userId = :userId") // Clear data for a specific user
    suspend fun clearData(userId: String)
}