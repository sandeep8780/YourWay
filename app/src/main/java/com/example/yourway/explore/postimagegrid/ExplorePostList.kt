package com.example.yourway.explore.postimagegrid

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

class ExplorePostList : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var userPosts: MutableList<Post> = mutableListOf()
    private var username: String? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val TAG = "ExplorePostList"

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
        val postId = arguments?.getString("postId")
        Log.d(TAG, postId.toString())

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

        if (!postId.isNullOrEmpty()) {
            // Fetch the post by postId
            Log.d(TAG, "Fetching post for postId: $postId")

            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val post = document.toObject(Post::class.java)
                        post?.let {
                            post.postId = document.id
                            userPosts.clear() // Clear existing posts before adding the new one
                            userPosts.add(post)
                            Log.d(TAG, "Added post with id: ${post.postId}")

                            // Scroll to the post if needed
                            val startIndex = userPosts.indexOfFirst { it.postId == postId }
                            Log.d(TAG, "Index of post with id $postId: $startIndex")
                            if (startIndex != -1) {
                                // Move the selected post to the top of the list
                                userPosts = (userPosts.subList(startIndex, userPosts.size) + userPosts.subList(0, startIndex)).toMutableList()
                                Log.d(TAG, "Rearranged userPosts to bring the post with id $postId to the top")
                            } else {
                                Log.d(TAG, "Post with id $postId not found in the list")
                            }

                            val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
                            postAdapter = PostAdapter(userPosts, sharedPreferencesHelper) { clickedPostId ->
                                // Handle post click
                                Log.d(TAG, "Post clicked: $clickedPostId")
                            }
                            recyclerView.adapter = postAdapter
                            Log.d(TAG, "PostAdapter set with ${userPosts.size} posts")
                        } ?: Log.e(TAG, "Post with id $postId not found")
                    } else {
                        Log.e(TAG, "Post with id $postId not found")
                    }
                    swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching post with id $postId: ${exception.message}")
                    swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
                }

        } else {
            Log.e(TAG, "PostId is null or empty")
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
        fun newInstance(username: String): ExplorePostList {
            val fragment = ExplorePostList()
            val args = Bundle()
            args.putString("username", username)
            fragment.arguments = args
            return fragment
        }
    }
}
