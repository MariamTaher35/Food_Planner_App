package com.example.foodplannerapplication.Models

data class CategoryResponse(val categories: List<Category>)
data class Category(val idCategory: String, val strCategory: String, val strCategoryThumb: String, val strCategoryDescription: String)

