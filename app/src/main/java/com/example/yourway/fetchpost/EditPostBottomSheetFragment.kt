package com.example.yourway.fetchpost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.yourway.R
import com.example.yourway.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class EditPostBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_POST_ID = "post_id"
        private const val ARG_POST_TITLE = "post_title"
        private const val ARG_POST_DESCRIPTION = "post_description"

        fun newInstance(post: Post): EditPostBottomSheetFragment {
            val fragment = EditPostBottomSheetFragment()
            val bundle = Bundle()
            bundle.putString(ARG_POST_ID, post.postId)
            bundle.putString(ARG_POST_TITLE, post.title)
            bundle.putString(ARG_POST_DESCRIPTION, post.description)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var postId: String? = null
    private var postTitle: String? = null
    private var postDescription: String? = null

    private lateinit var etTitle: EditText
    private lateinit var etDescription:EditText
    private lateinit var btnUpdatePost: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString(ARG_POST_ID)
            postTitle = it.getString(ARG_POST_TITLE)
            postDescription = it.getString(ARG_POST_DESCRIPTION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_edit_post_bottom_sheet, container, false)

        etTitle = view.findViewById(R.id.et_post_update_title)
        etDescription = view.findViewById(R.id.et_post_update_description)
        btnUpdatePost =view.findViewById(R.id.btn_update_post)

        etTitle.setText(postTitle)
        etDescription.setText(postDescription)

        btnUpdatePost.setOnClickListener {
            postTitle = etTitle.text.toString()
            postDescription = etDescription.text.toString()
            postId?.let { it1 -> updatePost(it1, postTitle!!, postDescription!!) }
        }
        return view
    }

    // Function to update the post document in Firestore
    private fun updatePost(postId: String, title: String, description: String) {
        val db = FirebaseFirestore.getInstance()

        // Create a map of the fields to update
        val postUpdates = hashMapOf(
            "title" to title,
            "description" to description
        )

        // Update the document with the specified postId
        db.collection("posts")
            .document(postId)
            .update(postUpdates as Map<String, Any>)
            .addOnSuccessListener {
                // Handle success, e.g., show a Toast message
                Toast("Post updated successfully!", requireContext())
                // Optionally, dismiss the bottom sheet or navigate away
                dismiss()
            }
            .addOnFailureListener { e ->
                // Handle failure, e.g., log the error or show an error message
                Log.e("Firestore", "Error updating post: ", e)
                Toast( "Failed to update post: ${e.message}",requireContext())
            }
    }

}
