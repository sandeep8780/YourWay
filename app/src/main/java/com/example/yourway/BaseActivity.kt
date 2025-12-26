package com.example.yourway

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.yourway.authentication.AuthenticationActivity
import com.example.yourway.chat.ChatBaseFragment
import com.example.yourway.explore.DisplayOtherUserProfile
import com.example.yourway.explore.ExploreFragment
import com.example.yourway.explore.postimagegrid.ExplorePostList
import com.example.yourway.fetchpost.HomeFeedPostListFragment
import com.example.yourway.fetchpost.UserPostList
import com.example.yourway.forum.ForumActivity
import com.example.yourway.userprofile.DisplayUserProfile
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BaseActivity : AppCompatActivity() {

    companion object {
        var isMessageNotificationServiceStarted = false // Global flag to track service start
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fcvBase: FragmentContainerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)

        bottomNavigationView = findViewById(R.id.bottom_nav)
        fcvBase = findViewById(R.id.fcv_base)
        drawerLayout = findViewById(R.id.drawer_layout_base)
        navView = findViewById(R.id.navigation_view_base)

        setDrawerLayout()

        // Only create notification channel and start the service once
        if (!isMessageNotificationServiceStarted) {
            createNotificationChannel()
            startChatListenerService()
            isMessageNotificationServiceStarted = true // Mark the service as started
        }

        // Check if intent has extras to open a specific fragment
        val fragmentToOpen = intent.getStringExtra("openFragment")

        if (fragmentToOpen == "chats") {
            // Open the chats fragment
            openChatsFragment()
        } else {
            // Load default fragment or activity content
            openDefaultFragment()
        }

        var addPostLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    setFragment(HomeFeedPostListFragment())


                }
                bottomNavigationView.selectedItemId = R.id.home
            }
        // Set the initial fragment
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    setFragment(HomeFeedPostListFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.explore -> {
                    setFragment(ExploreFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.addpost -> {
                    val intent = Intent(this@BaseActivity, AddPostActivity::class.java)
                    addPostLauncher.launch(intent)

                    return@setOnItemSelectedListener true
                }

                R.id.messages -> {
                    setFragment(ChatBaseFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.profile -> {
                    val fragment = DisplayUserProfile() // Replace with your Fragment class
                    val email = intent.getStringExtra("email") // Retrieve the email from the intent

                    // Create a Bundle to hold the email
                    val bundle = Bundle().apply {
                        putString("email", email) // Add the email to the Bundle
                    }

                    fragment.arguments = bundle
                    setFragment(fragment)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }


        createNotificationChannel()

    }

    private fun startChatListenerService() {
        val intent = Intent(this, ChatListenerService::class.java)
        Thread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }.start()
    }

    private fun openDefaultFragment() {
        setFragment(HomeFeedPostListFragment())
        Log.d("openfragment","home")
        bottomNavigationView.selectedItemId = R.id.home
    }

    private fun openChatsFragment() {
        Log.d("openfragment","chats")
        setFragment(ChatBaseFragment())
        bottomNavigationView.selectedItemId = R.id.messages
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "chat_notifications",
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setDrawerLayout() {
        // Drawer toggle (hamburger icon)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val sharedPreferencesHelper = SharedPreferencesHelper(applicationContext)
        val userProfile = sharedPreferencesHelper.getUserProfile()

        // Retrieve values from SharedPreferences
        val imageSrc = userProfile?.imgSrc
        val displayName = userProfile?.displayName
        val username = userProfile?.username

        val headerView = navView.getHeaderView(0)
        val textViewDisplayName: TextView = headerView.findViewById(R.id.header_user_display_name)
        val textViewUsername: TextView = headerView.findViewById(R.id.header_user_username)
        val imageViewProfile: ImageView = headerView.findViewById(R.id.header_user_profile_image)

        Glide.with(applicationContext)
            .load(imageSrc)
            .apply(RequestOptions().placeholder(R.drawable.placeholder_image)) // Set a placeholder
            .into(imageViewProfile)
        textViewDisplayName.text = displayName
        textViewUsername.text = username

        // Set navigation item listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_forums -> {
                    val intent = Intent(this@BaseActivity, ForumActivity::class.java)
                    startActivity(intent)
                    //close drawerLayout
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.menu_logout -> {
                    // Handle logout logic
                    logoutUser()
                    true
                }

                else -> false
            }
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        // Clear all user preferences data
        with(sharedPreferences.edit()) {
            clear() // Clears all data
            apply() // Apply changes asynchronously
        }

        // Redirect to Authentication Activity
        val intent = Intent(
            this,
            AuthenticationActivity::class.java
        ) // Replace with your actual Authentication Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear the activity stack
        startActivity(intent)
        finish() // Optionally finish the current activity
    }


    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fcv_base, fragment).addToBackStack("home").commit()
    }

    override fun onBackPressed() {
        // Get the current fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fcv_base)

        if (currentFragment is DisplayOtherUserProfile) {
            // If the current fragment is DisplayOtherUserProfile, replace it with ExploreFragment
            bottomNavigationView.selectedItemId = R.id.explore
        }
        else {
            // Otherwise, let the system handle the back press (default behavior)
           finish()
        }
    }

    fun replaceWithUserPostList(postId: String) {
        val fragment = UserPostList().apply {
            arguments = Bundle().apply {
                putString("postId", postId)  // Pass postId to UserPostList fragment
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_base, fragment)
            .addToBackStack("explore")  // Add to back stack if you want to allow back navigation
            .commit()
    }

    fun replaceWithExplorePostList(postId: String) {
        val fragment = ExplorePostList().apply {
            arguments = Bundle().apply {
                putString("postId", postId)  // Pass postId to UserPostList fragment
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_base, fragment)
            .addToBackStack("explore")
            .commit() // Add to back stack if you want to allow back navigation

    }
}