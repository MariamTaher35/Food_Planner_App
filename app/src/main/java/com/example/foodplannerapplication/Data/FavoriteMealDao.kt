package com.example.foodplannerapplication.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: FavoriteMeal)

    @Query("SELECT * FROM favorite_meals WHERE userId = :userId")
    fun getAll(userId: String): Flow<List<FavoriteMeal>>       // real time update ui

    @Delete
    suspend fun delete(meal: FavoriteMeal)

    @Query("SELECT * FROM favorite_meals WHERE id = :id AND userId = :userId")
    suspend fun getById(id: String, userId: String): FavoriteMeal?

    @Update
    suspend fun update(meal: FavoriteMeal)

    @Query("DELETE FROM favorite_meals WHERE userId = :userId")
    suspend fun clearData(userId: String)

    @Query("SELECT * FROM favorite_meals WHERE userId = :userId")
    fun getAllOnce(userId: String): List<FavoriteMeal>        // static data

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_meals WHERE id = :mealId AND userId = :userId)")
    suspend fun isMealFavorited(mealId: String, userId: String): Boolean
}
