package com.example.yourway

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yourway.post.BasicPost

class AddPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)



        // Check if savedInstanceState is null to avoid overlapping fragments when rotating the device
        if (savedInstanceState == null) {
            // Add BasicPostFragment to this activity
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_addpost, BasicPost())
                .commit()
        }
    }
    
}