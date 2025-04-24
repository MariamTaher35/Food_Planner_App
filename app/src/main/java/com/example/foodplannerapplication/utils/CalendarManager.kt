package com.example.foodplannerapplication.utils

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.foodplannerapplication.Data.PlannedMeal
import java.util.*

class CalendarManager(private val context: Context) {

    companion object {
        const val REQUEST_CODE = 1001
    }

    fun addMealToCalendar(meal: PlannedMeal) {
        if (!hasCalendarPermissions()) {
            requestCalendarPermissions()
            return
        }

        // Ensure meal details are not null before proceeding
        if (meal.mealName.isNullOrEmpty() || meal.dayOfWeek.isNullOrEmpty() || meal.mealTime.isNullOrEmpty()) {
            Toast.makeText(context, "Meal details are incomplete. Please check the meal name, day, and time.", Toast.LENGTH_SHORT).show()
            return
        }

        val calendarId = getWritableCalendarId()
        if (calendarId == null) {
            Toast.makeText(context, "No writable calendar found.", Toast.LENGTH_SHORT).show()
            return
        }

        val startTime = getMealStartTime(meal.dayOfWeek, meal.mealTime)
        if (startTime == null) {
            Toast.makeText(context, "Invalid meal date or time.", Toast.LENGTH_SHORT).show()
            return
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime.timeInMillis)
            put(CalendarContract.Events.DTEND, startTime.timeInMillis + 60 * 60 * 1000) // 1 hour duration
            put(CalendarContract.Events.TITLE, "Meal: ${meal.mealName}")
            put(CalendarContract.Events.DESCRIPTION, "Planned meal for ${meal.dayOfWeek} at ${meal.mealTime}.")
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

            if (uri != null) {
                Log.d("CalendarDebug", "Successfully inserted event: $uri")

                // Add a reminder for visibility/debugging
                val eventId = ContentUris.parseId(uri)
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.MINUTES, 10) // Reminder 10 minutes before
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)

                Toast.makeText(context, "Meal added to calendar.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("CalendarDebug", "Insert returned null — calendar may not be writable.")
                Toast.makeText(context, "Failed to add meal to calendar.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CalendarDebug", "Error inserting event: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun hasCalendarPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCalendarPermissions() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            REQUEST_CODE
        )
    }

    private fun getWritableCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )

        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND " +
                "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}"

        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val calendarId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                val calendarName = it.getString (it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                val accessLevel = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL))
                Log.d("CalendarDebug", "Found calendar: $calendarName (ID: $calendarId, Access: $accessLevel)")
                return calendarId
            }
        }

        Log.e("CalendarDebug", "No writable calendar found.")
        return null
    }

    private fun getMealStartTime(dayOfWeek: String, mealTime: String): Calendar? {
        val calendar = Calendar.getInstance()
        val timeParts = mealTime.split(":")
        if (timeParts.size != 2) return null

        val hour = timeParts[0].toIntOrNull() ?: return null
        val minute = timeParts[1].toIntOrNull() ?: return null

        // Set the calendar to the correct day of the week
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val targetDayOfWeek = when (dayOfWeek.toLowerCase(Locale.ROOT)) {
            "sunday" -> Calendar.SUNDAY
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            else -> return null
        }

        // Calculate the difference in days
        val daysDifference = (targetDayOfWeek - currentDayOfWeek + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, daysDifference)

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        Log.d("CalendarDebug", "Day of Week: $dayOfWeek, Meal Time: $mealTime")
        Log.d("CalendarDebug", "Resolved Meal Time: ${calendar.time}")
        return calendar
    }
}
