package com.example.yourway.userprofile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.yourway.R
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop // For circular cropping
import com.bumptech.glide.request.RequestOptions
import com.example.yourway.Toast
import com.example.yourway.authentication.AuthenticationActivity
import com.example.yourway.fetchpost.UserPostList
import com.example.yourway.forum.ForumActivity
import com.example.yourway.userprofile.postimagegrid.UserPostImageGridRVFragment
import com.example.yourway.userprofile.postimagegrid.VPAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DisplayUserProfile : Fragment() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var drawerLayout: DrawerLayout

    // Views
    private lateinit var usernameTextView: TextView
    private lateinit var displayNameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var linkTextView: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : ViewPager2
    private lateinit var vpAdapter: VPAdapter
    private lateinit var navView: NavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        // Initialize Views
        usernameTextView = view.findViewById(R.id.tv_display_profile_username)
        displayNameTextView = view.findViewById(R.id.tv_display_profile_displayName)
        bioTextView = view.findViewById(R.id.tv_display_profile_bio)
        linkTextView = view.findViewById(R.id.tv_display_profile_link)
        profileImageView = view.findViewById(R.id.iv_display_profile_profileImage)
        drawerLayout = view.findViewById(R.id.drawer_layout_profile)
        navView = view.findViewById(R.id.navigation_view_base)
        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_display_profile)

        // Get email from arguments
        val email = arguments?.getString("email") ?: return

        // Load user profile from SharedPreferences if available
        if (sharedPreferencesHelper.isUserProfileAvailable()) {
            loadUserProfileFromPreferences()
            setupVP(view, email)  // Call setupVP here
        } else {
            // Launch coroutine to fetch user profile
            CoroutineScope(Dispatchers.Main).launch {
                fetchUserProfileFromDatabase(email)
                setupVP(view, email)
            }
        }

        setDrawerLayout()

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            // On refresh, fetch the latest user profile data and update SharedPreferences
            CoroutineScope(Dispatchers.Main).launch {
                fetchUserProfileFromDatabase(email)
                swipeRefreshLayout.isRefreshing = false // Stop refresh animation
            }
        }
    }

    private fun setDrawerLayout() {
        val toggle = ActionBarDrawerToggle(
            requireActivity(), drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        val userProfile = sharedPreferencesHelper.getUserProfile()

        // Retrieve values from SharedPreferences
        val imageSrc = userProfile?.imgSrc
        val displayName = userProfile?.displayName
        val username = userProfile?.username

        val headerView = navView.getHeaderView(0)
        val textViewDisplayName: TextView = headerView.findViewById(R.id.header_user_display_name)
        val textViewUsername: TextView = headerView.findViewById(R.id.header_user_username)
        val imageViewProfile: ImageView = headerView.findViewById(R.id.header_user_profile_image)

        Glide.with(requireActivity())
            .load(imageSrc)
            .apply(RequestOptions().placeholder(R.drawable.placeholder_image)) // Set a placeholder
            .into(imageViewProfile)
        textViewDisplayName.text = displayName
        textViewUsername.text = username

        // Set navigation item listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.update_profile -> {
                    val userProfile = sharedPreferencesHelper.getUserProfile()
                    // Ensure that userProfile is not null before proceeding
                    if (userProfile != null) {
                        // Fetch email by username
                        getEmailByUsername(userProfile.username) { email ->
                            // Now that we have the email, we can proceed with creating the fragment
                            val fragment = CreateUserProfile().apply {
                                arguments = Bundle().apply {
                                    putString("email", email) // Pass the email retrieved from Firestore
                                }
                            }
                            // Perform the fragment transaction on the main thread
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fcv_base, fragment) // Use your FragmentContainerView ID
                                .addToBackStack(null) // Optional: add to back stack
                                .commit()
                        }
                    } else {
                        // Handle the case where userProfile is null
                        Toast("User profile not found",requireContext())
                    }
                    true
                }
                R.id.menu_forums -> {
                    val intent = Intent(requireContext(), ForumActivity::class.java)
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

    private fun getEmailByUsername(userProfile: String?, callback: (String?) -> Unit) {
        if (userProfile.isNullOrEmpty()) {
            callback(null) // Return null if the username is null or empty
            return
        }

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("usernametoemail")
            .whereEqualTo("username", userProfile)
            .limit(1) // Limit to 1 document since usernames should be unique

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = docRef.get().await()
                if (querySnapshot.isEmpty) {
                    callback(null) // No document found
                } else {
                    // Assuming the document has the username and email fields
                    val document = querySnapshot.documents[0]
                    val email = document.id // The document ID is the email
                    callback(email) // Return the email through the callback
                }
            } catch (e: Exception) {
                // Handle any errors
                callback(null) // Return null on error
            }
        }
    }

    private fun logoutUser() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPref", AppCompatActivity.MODE_PRIVATE)
        // Clear all user preferences data
        with(sharedPreferences.edit()) {
            clear() // Clears all data
            apply() // Apply changes asynchronously
        }

        // Redirect to Authentication Activity
        val intent = Intent(requireContext(), AuthenticationActivity::class.java) // Replace with your actual Authentication Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear the activity stack
        startActivity(intent)
        requireActivity().finish() // Optionally finish the current activity
    }

    private fun setupVP(view: View,email: String) {
        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        val userProfile = sharedPreferencesHelper.getUserProfile()
        val username = userProfile?.username

        tabLayout = view.findViewById(R.id.tabLayout_profile)
        viewPager = view.findViewById(R.id.viewPager_profile)

        vpAdapter = VPAdapter(childFragmentManager,lifecycle)
        vpAdapter.addFragment(UserPostImageGridRVFragment.newInstance(username.toString()),"Grid")
        vpAdapter.addFragment(UserPostList.newInstance(username.toString()),"List")
//        vpAdapter.addFragment(Business(),"Business")

        viewPager.adapter = vpAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = vpAdapter.getPageTitle(position)
        }.attach()
    }

    private fun loadUserProfileFromPreferences() {
        val userProfile = sharedPreferencesHelper.getUserProfile()
        if (userProfile != null) {
            // Update views with user profile data
            usernameTextView.text = userProfile.username
            displayNameTextView.text = userProfile.displayName
            bioTextView.text = userProfile.bio
            linkTextView.text = userProfile.link

            // Load image using Glide and make it round
            Glide.with(this)
                .load(userProfile.imgSrc)
                .placeholder(R.drawable.placeholder_image) // Placeholder image
                .transform(CircleCrop()) // Make image round
                .into(profileImageView)
        }
    }

    private suspend fun fetchUserProfileFromDatabase(email: String) {
        val username = getUsernameByEmail(email)
        if (username == null) {
            Toast("Username not found for email", requireContext())
            return
        }

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(username)

        // Add a real-time listener to the document
        docRef.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                // Handle error
                Toast("Exception: ${firebaseFirestoreException.message}", requireContext())
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Extract data from document
                val userProfile = UserProfile(
                    username = documentSnapshot.id,
                    displayName = documentSnapshot.getString("displayName") ?: "",
                    bio = documentSnapshot.getString("bio") ?: "",
                    link = documentSnapshot.getString("link") ?: "",
                    imgSrc = documentSnapshot.getString("imageSrc") ?: ""
                )

                // Save the new data to SharedPreferences
                sharedPreferencesHelper.saveUserProfile(userProfile)

                // Update the UI with the new data
                loadUserProfileFromPreferences()
            } else {
                // Handle the case where the document doesn't exist
                Toast("Profile not found", requireContext())
            }
        }
    }

    private suspend fun getUsernameByEmail(email: String): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Reference to the "usernametoemail" collection
            val document = db.collection("usernametoemail").document(email).get().await()
            if (document.exists()) {
                document.getString("username")
            } else {
                println("Document does not exist for this email")
                null // Document does not exist
            }
        } catch (e: Exception) {
            println("Error retrieving document: ${e.message}")
            null // Return null on failure
        }
    }
}

