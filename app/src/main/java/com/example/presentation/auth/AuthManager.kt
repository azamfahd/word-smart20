package com.example.presentation.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class UserSession(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val isDemo: Boolean = false
)

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.e("AuthManager", "Firebase is not initialized. Missing google-services.json?", e)
        null
    }
    private val credentialManager = CredentialManager.create(context)
    private var demoUser: UserSession? = null

    val currentUser: UserSession?
        get() = if (auth?.currentUser != null) {
            UserSession(
                uid = auth.currentUser!!.uid,
                displayName = auth.currentUser!!.displayName ?: "مستخدم مسجل",
                email = auth.currentUser!!.email,
                isDemo = false
            )
        } else {
            demoUser
        }

    fun signInAsDemoUser() {
        demoUser = UserSession(
            uid = "demo_offline_user_99",
            displayName = "مستخدم تجريبي",
            email = "demo@example.com",
            isDemo = true
        )
    }

    suspend fun signInWithGoogle(): Result<AuthResult> {
        return try {
            val serverClientIdId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (serverClientIdId == 0) {
                return Result.failure(Exception("لم يتم تكوين Firebase. يرجى إضافة ملف google-services.json"))
            }
            val serverClientId = context.getString(serverClientIdId)

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is GoogleIdTokenCredential) {
                val idToken = credential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth?.signInWithCredential(firebaseCredential)?.await()
                    ?: throw Exception("Firebase is not initialized")
                Result.success(authResult)
            } else {
                Result.failure(Exception("Unknown credential type"))
            }
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.e("AuthManager", "No Google accounts found", e)
            Result.failure(Exception("لا توجد حسابات جوجل مسجلة في هذا الجهاز. يرجى تسجيل الدخول لحساب جوجل في الإعدادات أولاً."))
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "Google Sign In Failed", e)
            Result.failure(Exception("فشل تسجيل الدخول: ${e.message}"))
        } catch (e: Exception) {
            Log.e("AuthManager", "Auth Failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
        demoUser = null
    }
}
