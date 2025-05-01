package com.example.pigeonsandseeds.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pigeonsandseeds.databinding.ActivityAuthBinding
import com.example.pigeonsandseeds.ui.business.BusinessDashboardActivity
import com.example.pigeonsandseeds.ui.user.UserDashboardActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()
        setupFacebookSignIn()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create()
        binding.facebookLoginButton.setReadPermissions("email", "public_profile")
        binding.facebookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken.token)
            }

            override fun onCancel() {
                Toast.makeText(this@AuthActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@AuthActivity, "Facebook login failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        binding.googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        binding.businessSignUpButton.setOnClickListener {
            // TODO: Implement business sign up
        }

        binding.userSignUpButton.setOnClickListener {
            // TODO: Implement user sign up
        }
    }

    private fun handleFacebookAccessToken(token: String) {
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    redirectToDashboard()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    redirectToDashboard()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redirectToDashboard() {
        // TODO: Check user type (business or customer) and redirect accordingly
        val intent = Intent(this, UserDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 9001
    }
} 