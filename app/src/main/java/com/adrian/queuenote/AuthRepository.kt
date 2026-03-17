package com.adrian.queuenote

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun register(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    // Actualizar nombre y/o foto
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val profileUpdates = userProfileChangeRequest {
                if (displayName != null) this.displayName = displayName
                if (photoUrl != null) this.photoUri = Uri.parse(photoUrl)
            }
            user.updateProfile(profileUpdates).await()
            user.reload().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGitHub(activity: Activity): Result<FirebaseUser?> {
        return try {
            val provider = OAuthProvider.newBuilder("github.com")
            val result = auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
