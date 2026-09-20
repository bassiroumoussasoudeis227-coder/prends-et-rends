package com.example.data

import android.util.Log
import com.example.model.LoanItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FirebaseLoansService {
    private val tag = "FirebaseLoansService"
    private val collectionName = "prends_et_rends_loans"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firebase Firestore unavailable: ${e.localizedMessage}")
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firestore != null

    suspend fun fetchAllLoans(): List<LoanItem> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection(collectionName).get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                LoanItem.fromFirestoreMap(data)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching loans from Firestore", e)
            emptyList()
        }
    }

    suspend fun saveLoan(loan: LoanItem): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection(collectionName).document(loan.id).set(loan.toFirestoreMap()).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error saving loan to Firestore", e)
            false
        }
    }

    suspend fun deleteLoan(loanId: String): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection(collectionName).document(loanId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error deleting loan from Firestore", e)
            false
        }
    }

    fun observeLoans(onChanged: (List<LoanItem>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection(collectionName)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Listen failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            doc.data?.let { LoanItem.fromFirestoreMap(it) }
                        }
                        onChanged(items)
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Could not register listener", e)
            null
        }
    }
}
