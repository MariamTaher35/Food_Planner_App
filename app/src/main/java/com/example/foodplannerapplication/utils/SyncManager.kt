package com.example.foodplannerapplication.utils

import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.Data.FavoriteMealDao
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.Data.PlannedMealDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SyncManager(
    private val favoriteMealDao: FavoriteMealDao,
    private val plannedMealDao: PlannedMealDao,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val currentUserId: String // Receive userId in constructor
) {

    private val userFavoritesRef = database.getReference("users/$currentUserId/favorites")
    private val userPlanRef = database.getReference("users/$currentUserId/plan")

    suspend fun syncFavorites() {
        Timber.d("Syncing favorites for user: $currentUserId")

        // Upload local favorites to Firebase
        favoriteMealDao.getAll(currentUserId).first().let { favorites ->
            val favoritesMap = favorites.associate { it.id to true }
            userFavoritesRef.setValue(favoritesMap).await()
            Timber.d("Uploaded ${favorites.size} favorites to Firebase for user: $currentUserId")
        }

        // Download Firebase favorites to Room
        val snapshot = userFavoritesRef.get().await()
        if (snapshot.exists()) {
            val firebaseFavorites = snapshot.children.mapNotNull { it.key?.let { mealId ->
                FavoriteMeal(mealId, "", null, null, null, null, null, guestMode = false, userId = currentUserId)
            }}
            val localFavorites = favoriteMealDao.getAll(currentUserId).first()
            val localFavoritesSet = localFavorites.map { it.id }.toSet()
            val firebaseFavoritesToInsert = firebaseFavorites.filter { !localFavoritesSet.contains(it.id) }
            firebaseFavoritesToInsert.forEach { favoriteMealDao.insert(it) }
            Timber.d("Downloaded ${firebaseFavoritesToInsert.size} new favorites from Firebase for user: $currentUserId")
        } else {
            Timber.d("No favorites data found on Firebase for user: $currentUserId")
        }
    }

    suspend fun syncPlan() {
        Timber.d("Syncing plan for user: $currentUserId")

        // Upload local plan to Firebase
        plannedMealDao.getAll(currentUserId).first().let { plannedMeals ->
            val planMap = mutableMapOf<String, MutableMap<String, MutableMap<String, Any>>>()
            plannedMeals.forEach { meal ->
                val dayMap = planMap.getOrPut(meal.dayOfWeek) { mutableMapOf() }
                val timeMap = dayMap.getOrPut(meal.mealTime) { mutableMapOf() }
                timeMap[meal.id] = mapOf( // Use meal.id as the unique key
                    "guestMode" to meal.guestMode,
                    "mealName" to (meal.mealName ?: ""),
                    "mealImageUrl" to (meal.mealImageUrl ?: "")
                )
            }
            userPlanRef.setValue(planMap).await()
            Timber.d("Uploaded ${plannedMeals.size} plan items to Firebase for user: $currentUserId")
        }

        // Download Firebase plan to Room
        val snapshot = userPlanRef.get().await()
        if (snapshot.exists()) {
            val firebasePlan = snapshot.children.flatMap { daySnapshot ->
                daySnapshot.children.flatMap { mealTimeSnapshot ->
                    mealTimeSnapshot.children.mapNotNull { mealIdSnapshot -> // Iterate through meal IDs
                        daySnapshot.key?.let { day ->
                            mealTimeSnapshot.key?.let { mealTime ->
                                mealIdSnapshot.key?.let { mealId -> // Get the meal ID
                                    val guestMode = mealIdSnapshot.child("guestMode").getValue(Boolean::class.java) ?: false
                                    val mealName = mealIdSnapshot.child("mealName").getValue(String::class.java)
                                    val mealImageUrl = mealIdSnapshot.child("mealImageUrl").getValue(String::class.java)
                                    PlannedMeal(mealId, day, mealTime, guestMode, mealName, mealImageUrl, currentUserId)
                                }
                            }
                        }
                    }
                }
            }

            val localPlan = plannedMealDao.getAll(currentUserId).first()
            val localPlanSet = localPlan.map { Triple(it.id, it.dayOfWeek, it.mealTime) }.toSet()
            val firebasePlanToInsert = firebasePlan.filter { Triple(it.id, it.dayOfWeek, it.mealTime) !in localPlanSet }
            firebasePlanToInsert.forEach { plannedMealDao.insert(it) }
            Timber.d("Downloaded ${firebasePlanToInsert.size} new plan items from Firebase for user: $currentUserId")

            // Handle deletions
            val firebasePlanSet = firebasePlan.map { Triple(it.id, it.dayOfWeek, it.mealTime) }.toSet()
            val localPlanToDelete = localPlan.filter { Triple(it.id, it.dayOfWeek, it.mealTime) !in firebasePlanSet }
            localPlanToDelete.forEach { plannedMealDao.delete(it) }
            Timber.d("Deleted ${localPlanToDelete.size} plan items based on Firebase for user: $currentUserId")

        } else {
            Timber.d("No plan data found on Firebase for user: $currentUserId")
        }
    }
}