package com.example.yourway.forum

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.chat.SharedPrefsHelper
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.firebase.firestore.FirebaseFirestore

class CreateForumFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var ibtnBack: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_forum, container, false)

        // Initialize UI elements
        titleEditText = view.findViewById(R.id.et_createforum_title)
        descriptionEditText = view.findViewById(R.id.et_createforum_description)
        submitButton = view.findViewById(R.id.btn_createforum_submit)
        ibtnBack = view.findViewById(R.id.ibtn_creatForum_back)

        ibtnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set click listener for submit button
        submitButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
            val userProfile = sharedPreferencesHelper.getUserProfile()
            val username = userProfile?.username

            if (title.isNotEmpty() && description.isNotEmpty()) {
                createNewForum(title, description, username.toString())
            } else {
                Toast("Title and Description cannot be empty", requireContext())
            }
        }

        return view
    }

    private fun createNewForum(title: String, description: String, username :String) {
        // Generate forum data
        val forum = Forum(
            title = title,
            description = description,
            createdBy = username,
            upvotes = 0,
            downvotes = 0,
            commentCount = 0,
            views = 0,
            createdAt = System.currentTimeMillis()
        )

        // Save forum data to Firestore or any other database
        val db = FirebaseFirestore.getInstance()
        db.collection("forums").add(forum)
            .addOnSuccessListener {
                Toast("Forum created successfully",requireContext())


            }
            .addOnFailureListener { e ->
                Toast("Failed to create forum: ${e.message}",requireContext())
            }
    }
}
