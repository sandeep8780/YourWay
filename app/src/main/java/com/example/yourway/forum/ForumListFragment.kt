package com.example.yourway.forum

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ForumListFragment : Fragment() {

    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var forumAdapter: ForumAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var buttonAddForum: Button
    private val forumList = mutableListOf<Forum>()

    private var lastVisible: DocumentSnapshot? = null
    private val forumsPerPage = 10 // Limit to 10 documents per fetch
    private val TAG = "ForumListFragment"

    private var isLoading = false // Loading state variable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forum_list, container, false)

        forumRecyclerView = view.findViewById(R.id.rv_forum_list)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_forum_list)
        forumRecyclerView.layoutManager = LinearLayoutManager(context)
        buttonAddForum = view.findViewById(R.id.btn_forumlist_add_forum)

        buttonAddForum.setOnClickListener {
            val fragment = CreateForumFragment()

            // Use the FragmentManager to replace the fragment
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_forum, fragment)
            transaction.addToBackStack(null) // Optional: adds the transaction to the back stack
            transaction.commit()
        }

        forumAdapter = ForumAdapter(forumList) { forumId ->
            navigateToForumDetail(forumId)
        }
        forumRecyclerView.adapter = forumAdapter

        // Initial fetch of forums
        fetchForums()

        // Set up SwipeRefreshLayout to refresh forum list
        swipeRefreshLayout.setOnRefreshListener {
            refreshForums()
        }

        // Set up endless scroll for pagination
        forumRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Trigger fetchMoreForums only if not already loading and total items are 10 or more
                if (!isLoading && totalItemCount >= 10 && totalItemCount <= (lastVisibleItem + 2)) {
                    fetchMoreForums()
                }
            }
        })

        return view
    }

    // Fetch the initial set of forums
    private fun fetchForums() {
        if (isLoading) return // Prevent multiple fetches
        isLoading = true // Set loading state

        Log.d(TAG, "Fetching forums...")
        FirebaseFirestore.getInstance().collection("forums")
            .orderBy("upvotes", Query.Direction.DESCENDING) // Filter by upvotes
            .orderBy("createdAt", Query.Direction.DESCENDING) // Filter by recent upload time
            .limit(forumsPerPage.toLong()) // Limit to 10 forums
            .get()
            .addOnSuccessListener { querySnapshot ->
                forumList.clear() // Clear the list for fresh fetch
                for (document in querySnapshot) {
                    val forum = document.toObject(Forum::class.java)
                    forum.id = document.id
                    forumList.add(forum)
                }
                forumAdapter.notifyDataSetChanged()
                Log.d(TAG, "Fetched ${forumList.size} forums")

                // Get the last visible document for pagination
                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                    Log.d(TAG, "Last visible document set for pagination")
                }

                isLoading = false // Reset loading state
                swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator
                Toast.makeText(requireContext(), "Forums loaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching forums: ", exception)
                isLoading = false // Reset loading state
                swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator
                Toast.makeText(requireContext(), "Failed to load forums", Toast.LENGTH_SHORT).show()
            }
    }

    // Refresh the forums (Pull to refresh)
    private fun refreshForums() {
        lastVisible = null // Reset lastVisible for a fresh fetch
        fetchForums()
    }

    // Fetch more forums when scrolling reaches the bottom (pagination)
    private fun fetchMoreForums() {
        if (lastVisible != null && !isLoading) {
            isLoading = true // Set loading state
            Log.d(TAG, "Fetching more forums...")

            FirebaseFirestore.getInstance().collection("forums")
                .orderBy("upvotes", Query.Direction.DESCENDING) // Filter by upvotes
                .orderBy("createdAt", Query.Direction.DESCENDING) // Filter by recent upload time
                .startAfter(lastVisible) // Start after the last fetched document
                .limit(forumsPerPage.toLong()) // Limit to 10 more forums
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val forum = document.toObject(Forum::class.java)
                        forum.id = document.id
                        forumList.add(forum)
                    }
                    forumAdapter.notifyDataSetChanged()
                    Log.d(TAG, "Fetched ${querySnapshot.size()} more forums")

                    // Update the last visible document for further pagination
                    if (querySnapshot.documents.isNotEmpty()) {
                        lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                        Log.d(TAG, "Updated last visible document for pagination")
                    } else {
                        Log.d(TAG, "No more forums to load")
                    }

                    isLoading = false // Reset loading state
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching more forums: ", exception)
                    isLoading = false // Reset loading state
                    Toast.makeText(requireContext(), "Failed to load more forums", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d(TAG, "No last visible document found, cannot fetch more forums")
        }
    }

    // Navigate to forum detail screen
    private fun navigateToForumDetail(forumId: String) {
        if (forumId.isEmpty()) {
            Log.e(TAG, "Cannot navigate to forum detail: forumId is empty")
            Toast.makeText(context, "Invalid Forum ID", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Navigating to forum detail with ID: $forumId")

        // Use the newInstance method to create the fragment
        val fragment = ForumDetailFragment.newInstance(forumId)

        // Use parentFragmentManager to replace the fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fcv_forum, fragment)
            .addToBackStack(null) // Add the transaction to the back stack
            .commit()
    }
}

