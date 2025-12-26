package com.example.yourway.userprofile.postimagegrid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.BaseActivity
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.explore.postimagegrid.Post
import com.example.yourway.explore.postimagegrid.PostAdapter
import com.example.yourway.fetchpost.UserPostList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query


class UserPostImageGridRVFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isFetching = false
    private var username: String? = null  // Variable to hold the passed username

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_image_grid_r_v, container, false)

        // Retrieve the passed username from the arguments
        username = arguments?.getString("username")

        recyclerView = view.findViewById(R.id.rv_explore_post_image_grid)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_explore_pig)

        setupRecyclerView()
        fetchPosts()

        swipeRefreshLayout.setOnRefreshListener {
            fetchPosts(isRefreshing = true)
        }

        return view
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(mutableListOf()) { post ->
            // Start FullPostActivity
            (activity as? BaseActivity)?.replaceWithUserPostList(post.id)
        }

        recyclerView.adapter = postAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 3)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == postAdapter.itemCount - 1) {
                    fetchPosts(isFetchingMore = true)
                }
            }
        })
    }
    private fun fetchPosts(isRefreshing: Boolean = false, isFetchingMore: Boolean = false) {
        Log.d("PostImageGridRVFragment", "fetchPosts called") // Log fetch call

        if (isFetching) return
        isFetching = true

        getRandomPostsFromFirestore { fetchedPosts ->
            Log.d("PostImageGridRVFragment", "Fetched posts: ${fetchedPosts.size}") // Log number of posts fetched

            when {
                isRefreshing -> {
                    postAdapter.refreshPosts(fetchedPosts)
                    swipeRefreshLayout.isRefreshing = false
                }
                isFetchingMore -> postAdapter.addPosts(fetchedPosts)
                else -> postAdapter.refreshPosts(fetchedPosts)
            }

            isFetching = false
        }
    }

    private fun getRandomPostsFromFirestore(onPostsFetched: (List<Post>) -> Unit) {
        Log.d("Firestore", "Fetching posts...")

        val posts = mutableListOf<Post>()
        val db = FirebaseFirestore.getInstance()
        var query: Query? = null

        // Apply filtering by username if available
        if (!username.isNullOrEmpty()) {
            query = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("username", username)
                .limit(50)
            Log.d("Firestore", "Filtering by username: $username")
        }

        query!!.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Log.d("Firestore", "Data fetched successfully. Number of posts: ${snapshot.size()}")
                    for (doc in snapshot) {
                        val post = doc.toObject(Post::class.java)
                        post.id = doc.id
                        Toast(post.id,requireContext())
                        posts.add(post)
                        Log.d("Firestore", "Fetched post: $post")  // Log each fetched post
                    }
                    onPostsFetched(posts)
                } else {
                    Log.d("Firestore", "No posts found")
                }
            }
            .addOnFailureListener { exception ->
                // Check if the error is related to Firestore requiring an index
                if (exception is FirebaseFirestoreException && exception.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    Log.e("Firestore", "Index required: ${exception.message}")
                } else {
                    Log.e("Firestore", "Error fetching posts: ${exception.message}")
                }
            }
    }



    companion object {
        // Method to instantiate fragment with username argument
        fun newInstance(username: String): UserPostImageGridRVFragment{
            val fragment = UserPostImageGridRVFragment()
            val args = Bundle()
            args.putString("username", username)
            fragment.arguments = args
            return fragment
        }
    }
}