package com.example.foodplannerapplication.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: FavoriteMeal)

    @Query("SELECT * FROM favorite_meals WHERE userId = :userId") // Filter by userId
    fun getAll(userId: String): Flow<List<FavoriteMeal>>

    @Delete
    suspend fun delete(meal: FavoriteMeal)

    @Query("SELECT * FROM favorite_meals WHERE id = :id AND userId = :userId") // Filter by userId
    suspend fun getById(id: String, userId: String): FavoriteMeal?

    @Update
    suspend fun update(meal: FavoriteMeal)

    @Query("DELETE FROM favorite_meals WHERE userId = :userId") // Clear data for a specific user
    suspend fun clearData(userId: String)
}