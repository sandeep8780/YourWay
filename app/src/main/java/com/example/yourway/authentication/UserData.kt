package com.example.yourway.authentication

import android.util.Log
import com.example.yourway.Toast
import com.google.firebase.auth.FirebaseAuth

data class UserData(val email: String, val password: String) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        const val USER_EXISTS = 1
        const val NEW_USER = 2
        const val SIGN_IN_SUCCESS = 3
        const val SIGN_IN_FAILED = 4
    }

    // Sign up a new user or check if the user already exists
    fun signUp(onCompleteListener: (Int) -> Unit) {
        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods ?: emptyList()
                if (signInMethods.isNotEmpty()) {
                    // User already exists
                    onCompleteListener(USER_EXISTS)
                } else {
                    // New user, proceed with sign-up
                    createUser(onCompleteListener)
                }
            } else {
                Log.e("UserData", "Error checking user existence: ${task.exception?.message}")
                onCompleteListener(SIGN_IN_FAILED)
            }
        }
    }

    private fun createUser(onCompleteListener: (Int) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { signUpTask ->
            if (signUpTask.isSuccessful) {
                onCompleteListener(NEW_USER)
            } else {
                Log.e("UserData", "Sign-up failed: ${signUpTask.exception?.message}")
                onCompleteListener(SIGN_IN_FAILED)
            }
        }
    }

    // Sign in existing user
    fun signIn(onCompleteListener: (Int) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserData", "Sign-in successful for user: $email")
                onCompleteListener(SIGN_IN_SUCCESS)
            } else {
                Log.e("UserData", "Sign-in failed: ${task.exception?.message}")
                onCompleteListener(SIGN_IN_FAILED)
            }
        }
    }
}
