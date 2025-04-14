package com.example.foodplannerapplication.Data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavoriteMeal::class, PlannedMeal::class], version = 4) // Increment the version to 4
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteMealDao(): FavoriteMealDao
    abstract fun plannedMealDao(): PlannedMealDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_planner_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Add the new migration
                    // .fallbackToDestructiveMigration() // Uncomment for testing
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Starting migration from version 1 to 2")
                try {
                    database.execSQL("ALTER TABLE planned_meals ADD COLUMN mealName TEXT NOT NULL DEFAULT 'Default Name'")
                    database.execSQL("ALTER TABLE planned_meals ADD COLUMN mealImageUrl TEXT NOT NULL DEFAULT 'Default URL'")
                    Log.d("AppDatabase", "Migration from version 1 to 2 complete")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Migration failed", e)
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE planned_meals ADD COLUMN mealName TEXT")
                database.execSQL("ALTER TABLE planned_meals ADD COLUMN mealImageUrl TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Starting migration from version 3 to 4")
                try {
                    // Add the userId column to the planned_meals table
                    database.execSQL("ALTER TABLE planned_meals ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                    // Add the userId column to the favorite_meals table
                    database.execSQL("ALTER TABLE favorite_meals ADD COLUMN userId TEXT NOT NULL DEFAULT ''")

                    // Recreate the tables with the new primary keys including userId
                    database.execSQL("CREATE TABLE IF NOT EXISTS `planned_meals_new` (`id` TEXT NOT NULL, `dayOfWeek` TEXT NOT NULL, `mealTime` TEXT NOT NULL, `guestMode` INTEGER NOT NULL, `mealName` TEXT, `mealImageUrl` TEXT, `userId` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`))")
                    database.execSQL("INSERT INTO `planned_meals_new` (`id`, `dayOfWeek`, `mealTime`, `guestMode`, `mealName`, `mealImageUrl`, `userId`) SELECT `id`, `dayOfWeek`, `mealTime`, `guestMode`, `mealName`, `mealImageUrl`, '' FROM `planned_meals`")
                    database.execSQL("DROP TABLE `planned_meals`")
                    database.execSQL("ALTER TABLE `planned_meals_new` RENAME TO `planned_meals`")

                    database.execSQL("CREATE TABLE IF NOT EXISTS `favorite_meals_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `imageUrl` TEXT, `originCountry` TEXT, `ingredients` TEXT, `steps` TEXT, `videoUrl` TEXT, `guestMode` INTEGER NOT NULL, `userId` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`))")
                    database.execSQL("INSERT INTO `favorite_meals_new` (`id`, `name`, `imageUrl`, `originCountry`, `ingredients`, `steps`, `videoUrl`, `guestMode`, `userId`) SELECT `id`, `name`, `imageUrl`, `originCountry`, `ingredients`, `steps`, `videoUrl`, `guestMode`, '' FROM `favorite_meals`")
                    database.execSQL("DROP TABLE `favorite_meals`")
                    database.execSQL("ALTER TABLE `favorite_meals_new` RENAME TO `favorite_meals`")

                    Log.d("AppDatabase", "Migration from version 3 to 4 complete")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Migration from version 3 to 4 failed", e)
                }
            }
        }
    }
}