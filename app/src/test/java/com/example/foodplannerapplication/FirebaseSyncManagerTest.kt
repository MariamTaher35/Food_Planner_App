package com.example.foodplannerapplication

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import com.nhaarman.mockitokotlin2.mock


class FirebaseSyncManagerTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var userDocument: DocumentReference
    private lateinit var favoritesCollection: CollectionReference
    private lateinit var mealDocument: DocumentReference
    private lateinit var syncManager: FirebaseSyncManager

    @Before
    fun setup() {
        firestore = mock()
        usersCollection = mock()
        userDocument = mock()
        favoritesCollection = mock()
        mealDocument = mock()

        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document("testUserId")).thenReturn(userDocument)
        whenever(userDocument.collection("favorites")).thenReturn(favoritesCollection)
        whenever(favoritesCollection.document("12345")).thenReturn(mealDocument)

        syncManager = FirebaseSyncManager(firestore)
    }

    @Test
    fun `test uploadFavoriteMeal success`() {
        val mealData = mapOf("name" to "Chicken Curry")
        syncManager.uploadFavoriteMeal("testUserId", "12345", mealData)
        verify(mealDocument).set(mealData)
    }

    @Test
    fun `test uploadFavoriteMeal failure`() {
        whenever(mealDocument.set(any())).thenThrow(RuntimeException("Firebase error"))
        val mealData = mapOf("name" to "Chicken Curry")
        try{
            syncManager.uploadFavoriteMeal("testUserId", "12345", mealData)
        } catch (e: Exception){
            assertEquals("Firebase error", e.message)
        }
    }
}

// Example FirebaseSyncManager class (simplified)
class FirebaseSyncManager(private val firestore: FirebaseFirestore) {

    fun uploadFavoriteMeal(userId: String, mealId: String, mealData: Map<String, Any>) {
        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(mealId)
            .set(mealData)
    }
}