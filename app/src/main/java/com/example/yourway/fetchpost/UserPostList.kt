package com.example.yourway.fetchpost

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.fetchpost.Post
import com.example.yourway.fetchpost.PostAdapter
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class UserPostList : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var userPosts: MutableList<Post> = mutableListOf()
    private var username: String? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val TAG = "UserPostList"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called") // Log lifecycle method

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_post_list, container, false)

        username = arguments?.getString("username")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.rv_userpostlist)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_userpostlist)
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh the data when swiped down
            refreshUserPosts()
        }

        Log.d(TAG, "RecyclerView initialized")

        // Fetch user posts
        fetchUserPosts()

        return view
    }

    private fun fetchUserPosts() {
        Log.d(TAG, "fetchUserPosts called")
        swipeRefreshLayout.isRefreshing = true // Start the refreshing indicator

        val db = FirebaseFirestore.getInstance()

        // Get postId from arguments if passed
        val postId = arguments?.getString("postId")
        Log.d(TAG, "PostId passed via arguments: $postId")

        if (username.isNullOrEmpty() && !postId.isNullOrEmpty()) {
            // Fetch the post by postId to get the associated username
            Log.d(TAG, "Username is null but postId is not null, fetching username for postId: $postId")

            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("username")
                        Log.d(TAG, "Username associated with postId $postId: $username")

                        // Fetch posts for the retrieved username
                        if (!username.isNullOrEmpty()) {
                            fetchPostsForUsername(username!!, postId)
                        } else {
                            Log.e(TAG, "Username is null after fetching postId")
                        }
                    } else {
                        Log.e(TAG, "Post with id $postId not found")
                    }
                    swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching post with id $postId: ${exception.message}")
                    swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
                }

        } else if (!username.isNullOrEmpty()) {
            // Fetch posts for the given username
            Log.d(TAG, "Fetching posts for username: $username")
            fetchPostsForUsername(username!!, postId)
        } else {
            Log.e(TAG, "Both username and postId are null or empty")
            swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
        }
    }

    private fun fetchPostsForUsername(username: String, postId: String?) {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("username", username)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Fetched ${result.size()} posts from Firestore")

                userPosts.clear() // Clear existing posts before adding new ones
                for (document in result) {
                    val post = document.toObject(Post::class.java)
                    post.postId = document.id
                    userPosts.add(post)
                    Log.d(TAG, "Added post with id: ${post.postId}")
                }

                // If postId is provided, scroll to the post
                postId?.let {
                    val startIndex = userPosts.indexOfFirst { it.postId == postId }
                    Log.d(TAG, "Index of post with id $postId: $startIndex")
                    if (startIndex != -1) {
                        // Move the selected post to the top of the list
                        userPosts = (userPosts.subList(startIndex, userPosts.size) + userPosts.subList(0, startIndex)).toMutableList()
                        Log.d(TAG, "Rearranged userPosts to bring the post with id $postId to the top")
                    } else {
                        Log.d(TAG, "Post with id $postId not found in the list")
                    }
                }

                val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
                // Initialize adapter and attach to RecyclerView
                postAdapter = PostAdapter(userPosts, sharedPreferencesHelper) { clickedPostId ->
                    // Handle post click
                    Log.d(TAG, "Post clicked: $clickedPostId")
                }
                recyclerView.adapter = postAdapter
                Log.d(TAG, "PostAdapter set with ${userPosts.size} posts")

                swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching posts: ${exception.message}")
                swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
            }
    }

    private fun refreshUserPosts() {
        // Re-fetch the user posts when the user swipes to refresh
        userPosts.clear() // Clear the current list
        fetchUserPosts() // Call the fetch method to get updated posts
    }

    companion object {
        // Method to instantiate fragment with username argument
        fun newInstance(username: String): UserPostList {
            val fragment = UserPostList()
            val args = Bundle()
            args.putString("username", username)
            fragment.arguments = args
            return fragment
        }
    }
}
