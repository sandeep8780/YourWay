package com.example.yourway.explore.userlist

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.R
import com.example.yourway.Toast
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserListFragment : Fragment() {

    private lateinit var userAdapter: UserListAdapter
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val handler = Handler()

    private lateinit var rvUserList : RecyclerView
    private lateinit var swipeRefreshLayout : SwipeRefreshLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast("ULCalled",requireContext())
        val view =  inflater.inflate(R.layout.fragment_user_list, container, false)

        rvUserList = view.findViewById(R.id.rv_explore_userlist)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_explore_userlist)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userAdapter = UserListAdapter(mutableListOf())
        rvUserList.adapter = userAdapter
        rvUserList.layoutManager = LinearLayoutManager(context)

        val searchText = arguments?.getString("searchText") ?: ""
        fetchUsers(searchText)

        swipeRefreshLayout.setOnRefreshListener {
            fetchUsers(searchText, isRefreshing = true)
        }
    }

    private fun fetchUsers(searchText: String, isRefreshing: Boolean = false) {
        val firestore = FirebaseFirestore.getInstance()
        val allUsers = mutableListOf<User>()

        // If the search text is empty, fetch recent users based on a created timestamp
        if (searchText.isEmpty()) {
            firestore.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Ensure you have a createdAt field
                .limit(20) // Limit to recent users
                .get()
                .addOnSuccessListener { documents ->
                    allUsers.addAll(documents.map {
                        User(username = it.id, displayName = it.getString("displayName") ?: "", imageSrc = it.getString("imageSrc") ?: "")
                    })
                    userAdapter.updateUsers(allUsers)
                    swipeRefreshLayout.isRefreshing = false
                }
                .addOnFailureListener {
                    Log.e("UserListFragment", "Error fetching recent users", it)
                    swipeRefreshLayout.isRefreshing = false
                }
        } else {
            // Fetch a larger set of users for client-side filtering
            firestore.collection("users") // Adjust this limit as needed
                .get()
                .addOnSuccessListener { documents ->
                    // Add all users fetched to the list
                    allUsers.addAll(documents.map {
                        User(username = it.id, displayName = it.getString("displayName") ?: "", imageSrc = it.getString("imageSrc") ?: "")
                    })

                    // Filter users by checking if their displayName or username contains the searchText
                    val filteredUsers = allUsers.filter {
                        (it.displayName?.contains(searchText, ignoreCase = true) == true) ||
                                (it.username?.contains(searchText, ignoreCase = true) == true)
                    }

                    // Update the user adapter with the filtered results
                    userAdapter.updateUsers(filteredUsers.distinctBy { it.username }) // Ensure distinct usernames
                    swipeRefreshLayout.isRefreshing = false
                }.addOnFailureListener {
                    Log.e("UserListFragment", "Error fetching users", it)
                    swipeRefreshLayout.isRefreshing = false
                }
        }
    }


}