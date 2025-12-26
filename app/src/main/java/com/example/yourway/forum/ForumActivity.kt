package com.example.yourway.forum

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.yourway.AddPostActivity
import com.example.yourway.R
import com.example.yourway.chat.ChatBaseFragment
import com.example.yourway.explore.ExploreFragment
import com.example.yourway.userprofile.DisplayUserProfile
import com.google.android.material.bottomnavigation.BottomNavigationView

class ForumActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fcvForum : FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forum)


        fcvForum = findViewById(R.id.fcv_forum)
        bottomNavigationView = findViewById(R.id.bnv_forum)



        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.forum -> {
                    setFragment(ForumListFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.create -> {
                    setFragment(CreateForumFragment())
                    return@setOnItemSelectedListener true
                }

            }
            false
        }
        bottomNavigationView.selectedItemId = R.id.forum

    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fcv_forum, fragment).commit()
    }
}