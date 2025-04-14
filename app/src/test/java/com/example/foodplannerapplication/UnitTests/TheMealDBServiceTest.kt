package com.example.foodplannerapplication.UnitTests

import com.example.foodplannerapplication.Models.Area
import com.example.foodplannerapplication.Models.Category
import com.example.foodplannerapplication.Models.Meal
import com.example.foodplannerapplication.Models.TheMealDBService
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader

class TheMealDBServiceTest {

    private lateinit var service: TheMealDBService

    @Before
    fun setUp() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(TheMealDBService::class.java)
    }

    @Test
    fun getCategories_success() {
        val json = readJsonFile("categories.json")
        val response = Gson().fromJson(json, CategoryResponse::class.java)
        val categories = response.categories

        assertEquals(1, categories.size)
        assertEquals("Beef", categories[0].strCategory)
    }

    @Test
    fun getAreas_success() {
        val json = readJsonFile("areas.json")
        val response = Gson().fromJson(json, AreaResponse::class.java)
        val areas = response.meals

        assertEquals(1, areas.size)
        assertEquals("American", areas[0].strArea)
    }

    @Test
    fun getMealsByCategory_success() {
        val json = readJsonFile("mealsByCategory.json")
        val response = Gson().fromJson(json, MealListResponse::class.java)
        val meals = response.meals

        assertEquals(1, meals.size)
        assertEquals("Beef and Mustard Pie", meals[0].strMeal)
    }

    @Test
    fun getMealsByArea_success() {
        val json = readJsonFile("mealsByArea.json")
        val response = Gson().fromJson(json, MealListResponse::class.java)
        val meals = response.meals

        assertEquals(1, meals.size)
        assertEquals("Beef and Oyster pie", meals[0].strMeal)
    }

    @Test
    fun getMealsByIngredient_success() {
        val json = readJsonFile("mealsByIngredient.json")
        val response = Gson().fromJson(json, MealListResponse::class.java)
        val meals = response.meals

        assertEquals(1, meals.size)
        assertEquals("Chicken Congee", meals[0].strMeal)
    }

    @Test
    fun getMealDetails_success() {
        val json = readJsonFile("mealDetails.json")
        val response = Gson().fromJson(json, MealListResponse::class.java)
        val meals = response.meals

        assertEquals(1, meals.size)
        assertEquals("Teriyaki Chicken Rice", meals[0].strMeal)
        assertEquals("Soy Sauce", meals[0].strIngredient1)
        assertEquals("1/2 cup", meals[0].strMeasure1)
    }

    @Test
    fun getRandomMeal_success() {
        val json = readJsonFile("randomMeal.json")
        val response = Gson().fromJson(json, MealListResponse::class.java)
        val meals = response.meals

        assertEquals(1, meals.size)
        assertEquals("Beef Wellington", meals[0].strMeal)
    }

    private fun readJsonFile(fileName: String): String {
        println("Classpath: ${System.getProperty("java.class.path")}") // Add this line
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")
        return InputStreamReader(inputStream).readText()
    }

    // Data classes to match JSON structures
    data class CategoryResponse(val categories: List<Category>)
    data class AreaResponse(val meals: List<Area>)
    data class MealListResponse(val meals: List<Meal>)
}