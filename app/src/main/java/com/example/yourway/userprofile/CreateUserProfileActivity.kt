package com.example.yourway.userprofile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yourway.R
import com.example.yourway.Toast

class CreateUserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_user_profile)


        // Check if the fragment is already added to avoid overlapping on configuration changes
        if (savedInstanceState == null) {

            val fragment = CreateUserProfile() // Replace with your Fragment class
            val email = intent.getStringExtra("email") // Retrieve the email from the intent

            // Create a Bundle to hold the email
            val bundle = Bundle().apply {
                putString("email", email) // Add the email to the Bundle
            }

            fragment.arguments = bundle // Set the Bundle as arguments for the fragment

            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_cup, fragment)
                .commit()
        }
    }
}