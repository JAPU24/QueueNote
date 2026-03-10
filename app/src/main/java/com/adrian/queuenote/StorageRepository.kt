package com.adrian.queuenote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProfilePicture(userId: String, uri: Uri): Result<String> {
        return try {
            val fileRef = storageRef.child("profile_pictures/$userId.jpg")
            fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
