package com.example.presentation.cloud

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Base64
import java.util.Date

data class CloudDocument(
    val id: String = "",
    val title: String = "",
    val lastModified: Long = 0L,
    val dataBase64: String = ""
)

class CloudSyncManager {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "Firebase is not initialized. Cloud sync will be disabled.", e)
            null
        }
    }

    companion object {
        // Shared in-memory simulation cache for Demo/Offline users
        private val demoCloudDocsCache = mutableMapOf<String, MutableMap<String, CloudDocument>>()
    }

    suspend fun saveDocument(userId: String, docId: String, title: String, docxBytes: ByteArray) {
        val base64Data = Base64.encodeToString(docxBytes, Base64.DEFAULT)
        val cloudDoc = CloudDocument(
            id = docId,
            title = title,
            lastModified = Date().time,
            dataBase64 = base64Data
        )

        val fs = firestore
        if (fs == null || userId == "demo_offline_user_99") {
            val userDocs = demoCloudDocsCache.getOrPut(userId) { mutableMapOf() }
            userDocs[docId] = cloudDoc
            Log.d("CloudSync", "Saved document to local simulated cloud")
            return
        }
        
        try {
            fs.collection("users").document(userId).collection("documents").document(docId)
                .set(cloudDoc).await()
            Log.d("CloudSync", "Document saved to cloud successfully")
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to save document to cloud", e)
            throw e
        }
    }

    suspend fun getDocuments(userId: String): List<CloudDocument> {
        val fs = firestore
        if (fs == null || userId == "demo_offline_user_99") {
            return demoCloudDocsCache[userId]?.values?.toList() ?: emptyList()
        }

        return try {
            val snapshot = fs.collection("users").document(userId).collection("documents")
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(CloudDocument::class.java) }
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to get documents from cloud", e)
            emptyList()
        }
    }

    suspend fun deleteDocument(userId: String, docId: String) {
        val fs = firestore
        if (fs == null || userId == "demo_offline_user_99") {
            demoCloudDocsCache[userId]?.remove(docId)
            return
        }

        try {
            fs.collection("users").document(userId).collection("documents").document(docId)
                .delete().await()
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to delete document from cloud", e)
        }
    }
}
