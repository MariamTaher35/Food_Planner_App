package com.example.foodplannerapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.foodplannerapplication.Activities.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealOfDayFragmentTest {

    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testMealDetailsDisplayed() {
        onView(withId(R.id.mealNameTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.mealImageView)).check(matches(isDisplayed()))
        onView(withId(R.id.mealCategoryTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.mealAreaTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.mealInstructionsTextView)).check(matches(isDisplayed()))
    }
}