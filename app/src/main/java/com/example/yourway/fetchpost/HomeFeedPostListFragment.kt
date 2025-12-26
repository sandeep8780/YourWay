package com.example.yourway.fetchpost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFeedPostListFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val postList = mutableListOf<Post>()

    private var lastVisiblePost: DocumentSnapshot? = null

    private val pageSize = 40

    private val TAG = "HomeFeedPostListFragment" // Log tag
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_feed_post_list, container, false)

        postRecyclerView = view.findViewById(R.id.rv_homefeed)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_homefeed_post)

        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList,sharedPreferencesHelper) { postId -> openPostFragment(postId) }
        postRecyclerView.adapter = postAdapter

        fetchPosts()

        swipeRefreshLayout.setOnRefreshListener {
            refreshPosts()
        }

        // Infinite scroll listener for loading more posts
        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount

                val lastVisiblePost = layoutManager.itemCount
                // Trigger fetchMoreForums only if not already loading and total items are 10 or more
                if (!isLoading && totalItemCount >= 40 && totalItemCount <= (lastVisiblePost + 2)) {
                    fetchMorePosts()
                }
            }
        })

        fetchPosts()

        return view
    }

    private fun refreshPosts() {
        lastVisiblePost = null
        fetchPosts()
    }

    private fun fetchPosts() {
        if (isLoading) return
        isLoading = true
        Log.d(TAG, "Fetching initial posts...")

        FirebaseFirestore.getInstance().collection("posts")
            .orderBy("likes", Query.Direction.DESCENDING)
            .orderBy("commentCount", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                postList.clear()

                for (document in snapshot) {
                    val post = document.toObject(Post::class.java)
                    post.postId = document.id
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()

                if (snapshot.documents.isNotEmpty()) {
                    lastVisiblePost = snapshot.documents[snapshot.size() - 1]
                    Log.d(TAG, "Last visible document set for pagination")
                }
                isLoading = false

                swipeRefreshLayout.isRefreshing = false

            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching forums: ", exception)
                isLoading = false // Reset loading state
                swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator
                Toast("Failed to load forums", requireContext())
            }
    }

    private fun fetchMorePosts() {
        if (lastVisiblePost != null && !isLoading) {


            isLoading = true
            Log.d(TAG, "Fetching more posts after ${lastVisiblePost?.id}")

            // Fetch the next batch of posts after the last visible one
            FirebaseFirestore.getInstance().collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost) // Change to startAfter
                .limit(pageSize.toLong())
                .get()
                .addOnSuccessListener { snapshot ->
                    for (document in snapshot) {
                        val post = document.toObject(Post::class.java)
                        post.postId = document.id
                        postList.add(post)
                    }

                    postAdapter.notifyDataSetChanged()

                    if (snapshot.documents.isNotEmpty()) {
                        lastVisiblePost = snapshot.documents[snapshot.size() - 1]
                        Log.d(TAG, "Updated last visible document for pagination")
                    } else {
                        Log.d(TAG, "No more forums to load")
                    }
                    isLoading = false

                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    isLoading = false
                    Toast("Error fetching more posts: ${exception.message}", requireContext())
                    Log.e(TAG, "Error fetching more posts: ${exception.message}")
                    isLoading = false
                }
        } else {
            Log.d(TAG, "No last visible document found, cannot fetch more posts")
        }
    }

        private fun openPostFragment(postId: String) {
            val postFragment = PostFragment.newInstance(postId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcv_base, postFragment)
                .addToBackStack(null)
                .commit()
        }
    }
