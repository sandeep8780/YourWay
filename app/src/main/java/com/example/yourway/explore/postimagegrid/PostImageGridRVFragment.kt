package com.example.yourway.explore.postimagegrid

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostImageGridRVFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isFetching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_image_grid_r_v, container, false)
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

            (activity as? BaseActivity)?.replaceWithExplorePostList(post.id)

        }


        //in full post use :
//        val post = intent.getParcelableExtra<Post>("post")
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

            if (isRefreshing) {
                postAdapter.refreshPosts(fetchedPosts)
                swipeRefreshLayout.isRefreshing = false
            } else if (isFetchingMore) {
                postAdapter.addPosts(fetchedPosts)
            } else {
                postAdapter.refreshPosts(fetchedPosts)
            }

            isFetching = false
        }
    }

    private fun getRandomPostsFromFirestore(onPostsFetched: (List<Post>) -> Unit) {
        Toast("Fetching...", requireContext())


        val posts = mutableListOf<Post>()
        FirebaseFirestore.getInstance().collection("posts")
//            .whereArrayContains("imageUrls", true)
            .orderBy("likes",Query.Direction.DESCENDING)
            .orderBy("commentCount",Query.Direction.DESCENDING)
            .orderBy("timestamp",Query.Direction.DESCENDING)
            .limit(50)

            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Toast("Data fetched...", requireContext())
                    for (doc in snapshot) {
                        val post = doc.toObject(Post::class.java)
                        post.id = doc.id
                        posts.add(post)

                        Log.d("Post", "Fetched post: $post")  // Log each fetched post
                    }
                    onPostsFetched(posts)
                } else {
                    Toast("No post found...", requireContext())
                }
            }
            .addOnFailureListener { exception ->
                Toast("Error: ${exception.message}...", requireContext())
            }
    }

}
