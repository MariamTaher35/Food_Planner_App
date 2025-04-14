package com.example.foodplannerapplication.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodplannerapplication.Data.AppDatabase
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.ActivityLoginBinding
import com.example.foodplannerapplication.utils.SessionManager
import com.example.foodplannerapplication.utils.SyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager
    private lateinit var appDatabase: AppDatabase
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        sessionManager = SessionManager(this)
        appDatabase = AppDatabase.getInstance(this)
        authManager = AuthManager(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Automatic Login Check
        if (authManager.isLoggedIn()) {
            navigateToMainActivity(sessionManager.getUserId() ?: auth.currentUser?.uid ?: "")
        } else if (authManager.isGuestMode()) {
            navigateToMainActivity(SessionManager.GUEST_USER_ID) // Use the constant for guest user
        }

        binding.loginButton.setOnClickListener {
            signInWithEmailAndPassword()
        }

        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.signupTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun signInWithEmailAndPassword() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Timber.d("Email login successful: ${auth.currentUser?.email}")
                        authManager.setLoggedIn(true)
                        authManager.setGuestMode(false)
                        navigateToMainActivity(auth.currentUser!!.uid)
                    } else {
                        Timber.w("Email login failed: ${task.exception?.message}")
                        Toast.makeText(baseContext, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, getString(R.string.enter_email_password), Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account?.idToken != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                } else {
                    Timber.w("Google sign in failed: ID token null.")
                    Toast.makeText(this, getString(R.string.google_sign_in_failed_id_token), Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Timber.w("Google sign in failed: ${e.message}")
                Toast.makeText(this, getString(R.string.google_sign_in_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Timber.d("Google login successful: ${auth.currentUser?.email}")
                    authManager.setLoggedIn(true)
                    authManager.setGuestMode(false)
                    navigateToMainActivity(auth.currentUser!!.uid)
                } else {
                    Timber.w("Google login failed: ${task.exception?.message}")
                    Toast.makeText(baseContext, getString(R.string.google_sign_in_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity(userId: String) {
        sessionManager.saveUserId(userId)
        syncData(userId) // Pass userId to syncData
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun syncData(userId: String) {
        val syncManager = SyncManager(
            appDatabase.favoriteMealDao(),
            appDatabase.plannedMealDao(),
            auth,
            FirebaseDatabase.getInstance(),
            userId // Pass userId to SyncManager
        )
        lifecycleScope.launch {
            syncManager.syncFavorites()
            syncManager.syncPlan()
        }
    }
}