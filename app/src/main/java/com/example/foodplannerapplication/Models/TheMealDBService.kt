package com.example.foodplannerapplication.Models

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDBService {

    @GET("random.php")
    fun getRandomMeal(): Call<MealsResponse>

    @GET("categories.php")
    fun getCategories(): Call<CategoryResponse>

    @GET("list.php?a=list")
    fun getAreas(): Call<AreaResponse>

    @GET("filter.php")
    fun getMealsByCategory(@Query("c") category: String): Call<MealsResponse>

    @GET("filter.php")
    fun getMealsByArea(@Query("a") area: String): Call<MealsResponse>

    @GET("lookup.php")
    fun getMealDetails(@Query("i") mealId: String): Call<MealsResponse>

    @GET("search.php")
    fun searchMeals(@Query("s") searchTerm: String): Call<MealsResponse>

    companion object {
        private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

        fun create(): TheMealDBService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(TheMealDBService::class.java)
        }
    }
}
