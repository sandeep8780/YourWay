package com.example.yourway

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import com.example.yourway.userprofile.CreateUserProfile
import com.example.yourway.userprofile.DisplayUserProfile

class MainActivity : AppCompatActivity() {


    private lateinit var fragmentContainerView: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        fragmentContainerView = findViewById(R.id.fcv_main)




        if (savedInstanceState == null) {

            val fragment = DisplayUserProfile() // Replace with your Fragment class
            val email = intent.getStringExtra("email") // Retrieve the email from the intent

            // Create a Bundle to hold the email
            val bundle = Bundle().apply {
                putString("email", email) // Add the email to the Bundle
            }

            fragment.arguments = bundle // Set the Bundle as arguments for the fragment

            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_main, fragment)
                .commit()
        }

    }
}