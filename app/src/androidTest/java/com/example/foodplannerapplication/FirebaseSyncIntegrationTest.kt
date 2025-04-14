package com.example.foodplannerapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseSyncIntegrationTest {

    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setup() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
        firestore = FirebaseFirestore.getInstance()
    }

    @After
    fun cleanUp() {
        val latch = CountDownLatch(1) // Add a latch to wait for cleanup to complete

        firestore.collection("users").get().addOnSuccessListener { querySnapshot ->
            val deleteTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>() // List to store delete tasks

            for (document in querySnapshot.documents) {
                deleteTasks.add(firestore.collection("users").document(document.id).delete()) // Add delete tasks to list
            }

            com.google.android.gms.tasks.Tasks.whenAllComplete(deleteTasks) // Combine all delete tasks
                .addOnCompleteListener { latch.countDown() } // Signal completion of cleanup
        }.addOnFailureListener{
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS) // Wait for cleanup to complete (timeout)
    }

    @Test
    fun testFavoriteMealSynchronization() { // Changed function name
        val userId = "integrationTestUser"
        val mealId = "integrationTestMeal"
        val mealData = mapOf("name" to "Integration Test Meal")

        val setLatch = CountDownLatch(1)
        val getLatch = CountDownLatch(1)

        firestore.collection("users").document(userId).collection("favorites").document(mealId).set(mealData)
            .addOnCompleteListener { setLatch.countDown() }

        setLatch.await(5, TimeUnit.SECONDS) // wait for set to complete.

        firestore.collection("users").document(userId).collection("favorites").document(mealId).get()
            .addOnSuccessListener { documentSnapshot ->
                val retrievedMealData = documentSnapshot.data
                assertEquals(mealData, retrievedMealData)
                getLatch.countDown()
            }

        getLatch.await(5, TimeUnit.SECONDS) // wait for get to complete.

    }
}