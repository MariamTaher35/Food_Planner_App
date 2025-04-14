package com.example.foodplannerapplication.utils



import android.content.ContentValues

import android.content.Context

import android.provider.CalendarContract

import android.widget.Toast

import com.example.foodplannerapplication.Data.PlannedMeal

import java.util.*



class CalendarManager(private val context: Context) {



    fun addMealToCalendar(meal: PlannedMeal) {

        val calendarId = getCalendarId()

        if (calendarId == null) {

            Toast.makeText(context, "No calendar found.", Toast.LENGTH_SHORT).show()

            return

        }



        val startTime = getMealStartTime(meal.dayOfWeek, meal.mealTime)

        if (startTime == null) {

            Toast.makeText(context, "Invalid date or time.", Toast.LENGTH_SHORT).show()

            return

        }



        val values = ContentValues().apply {

            put(CalendarContract.Events.DTSTART, startTime.timeInMillis)

            put(CalendarContract.Events.DTEND, startTime.timeInMillis + 60 * 60 * 1000) // 1 hour event

            put(CalendarContract.Events.TITLE, "Meal: ${meal.mealName}")

            put(CalendarContract.Events.DESCRIPTION, "Planned meal for ${meal.dayOfWeek} at ${meal.mealTime}.")

            put(CalendarContract.Events.CALENDAR_ID, calendarId)

            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

        }



        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

        if (uri != null) {

            Toast.makeText(context, "Meal added to calendar.", Toast.LENGTH_SHORT).show()

        } else {

            Toast.makeText(context, "Failed to add meal to calendar.", Toast.LENGTH_SHORT).show()

        }

    }



    private fun getCalendarId(): Long? {

        val projection = arrayOf(CalendarContract.Calendars._ID)

        val cursor = context.contentResolver.query(

            CalendarContract.Calendars.CONTENT_URI,

            projection,

            null,

            null,

            null

        )

        cursor?.use {

            if (it.moveToFirst()) {

                return it.getLong(0)

            }

        }

        return null

    }



    private fun getMealStartTime(dayOfWeek: String, mealTime: String): Calendar? {

        val dayOfWeekMap = mapOf(

            "Monday" to Calendar.MONDAY,

            "Tuesday" to Calendar.TUESDAY,

            "Wednesday" to Calendar.WEDNESDAY,

            "Thursday" to Calendar.THURSDAY,

            "Friday" to Calendar.FRIDAY,

            "Saturday" to Calendar.SATURDAY,

            "Sunday" to Calendar.SUNDAY

        )



        val day = dayOfWeekMap[dayOfWeek] ?: return null



        val timeParts = mealTime.split(":")

        if (timeParts.size != 2) return null



        val hour = timeParts[0].toIntOrNull() ?: return null

        val minute = timeParts[1].toIntOrNull() ?: return null



        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_WEEK, day)

        calendar.set(Calendar.HOUR_OF_DAY, hour)

        calendar.set(Calendar.MINUTE, minute)

        calendar.set(Calendar.SECOND, 0)

        calendar.set(Calendar.MILLISECOND, 0)



// Adjust for past days

        val today = Calendar.getInstance()

        if (calendar.timeInMillis < today.timeInMillis) {

            calendar.add(Calendar.WEEK_OF_YEAR, 1)

        }



        return calendar

    }

}