// RoomDaoTest.kt
package com.example.foodplannerapplication

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.Data.FavoriteMeal
import com.example.foodplannerapplication.Data.FavoriteMealDao
import com.example.foodplannerapplication.Data.PlannedMeal
import com.example.foodplannerapplication.Data.PlannedMealDao
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var favoriteMealDao: FavoriteMealDao
    private lateinit var plannedMealDao: PlannedMealDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        favoriteMealDao = db.favoriteMealDao()
        plannedMealDao = db.plannedMealDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFavoriteMealDao() = runBlocking {
        val meal1 = FavoriteMeal(id = "1", name = "Test Meal 1", imageUrl = "image1", originCountry = "Country1", ingredients = "Ingredients1", steps = "Instructions1", videoUrl = "Youtube1", guestMode = false)
        val meal2 = FavoriteMeal(id = "2", name = "Test Meal 2", imageUrl = "image2", originCountry = "Country2", ingredients = "Ingredients2", steps = "Instructions2", videoUrl = "Youtube2", guestMode = false)

        // Remove the second parameter (false) from insert calls
        favoriteMealDao.insert(meal1)
        favoriteMealDao.insert(meal2)

        val allMeals = favoriteMealDao.getAll(false).first()
        println("Type of allMeals: ${allMeals::class.java.typeName}") // Debugging line
        assertThat(allMeals, hasSize(2))
        assertThat(allMeals, containsInAnyOrder(meal1, meal2))

        val retrievedMeal = favoriteMealDao.getById("1", false)
        assertEquals(meal1, retrievedMeal)

        favoriteMealDao.delete(meal1)

        val remainingMeals = favoriteMealDao.getAll(false).first()
        assertEquals(1, remainingMeals.size)
        assertTrue(remainingMeals.contains(meal2))
    }

    @Test
    fun testPlannedMealDao() = runBlocking {
        val meal1 = PlannedMeal(mealId = "1", dayOfWeek = "Monday", mealTime = "Lunch", guestMode = false)
        val meal2 = PlannedMeal(mealId = "2", dayOfWeek = "Tuesday", mealTime = "Dinner", guestMode = false)

        // Remove the second parameter (false) from insert calls
        plannedMealDao.insert(meal1)
        plannedMealDao.insert(meal2)

        val mondayMeals = plannedMealDao.getByDayOfWeek("Monday", false).first()
        println("Type of mondayMeals: ${mondayMeals::class.java.typeName}") // Debugging line
        val insertedMeal1 = mondayMeals.first()

        val tuesdayMeals = plannedMealDao.getByDayOfWeek("Tuesday", false).first()
        println("Type of tuesdayMeals: ${tuesdayMeals::class.java.typeName}") // Debugging line
        val insertedMeal2 = tuesdayMeals.first()

        val allMeals = plannedMealDao.getAll(false).first()
        println("Type of allMeals (Planned): ${allMeals::class.java.typeName}") // Debugging line
        assertThat(allMeals, hasSize(2))
        assertThat(allMeals, containsInAnyOrder(insertedMeal1, insertedMeal2))

        assertEquals(1, mondayMeals.size)
        assertTrue(mondayMeals.contains(insertedMeal1))

        plannedMealDao.delete(insertedMeal1)

        val remainingMeals = plannedMealDao.getAll(false).first()
        assertEquals(1, remainingMeals.size)
        assertTrue(remainingMeals.contains(insertedMeal2))
    }}