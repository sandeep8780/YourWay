package com.example.yourway.fetchpost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var etComment: EditText
    private lateinit var ibtnSendComment: ImageButton
    private lateinit var ibtnCancel: ImageButton

    companion object {
        private const val ARG_POST_ID = "postId"
        private const val TAG = "CommentFragment" // Tag for logging

        fun newInstance(postId: String): CommentBottomSheetFragment {
            return CommentBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_comment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View initialized")

        setupRecyclerView(view)
        setupCommentsSendingView(view)
        setupCancel(view)

        val postId = arguments?.getString(ARG_POST_ID)
        if (postId != null) {
            Log.d(TAG, "Fetching comments for post ID: $postId")
            fetchComments(postId)
        } else {
            Log.e(TAG, "Post ID is null!")
        }
    }

    private fun setupCancel(view: View) {
        ibtnCancel = view.findViewById(R.id.ibtn_comment_et_cancel)

        etComment.addTextChangedListener {
            if (!etComment.text.isNullOrEmpty()) {
                ibtnCancel.visibility = View.VISIBLE
            } else {
                ibtnCancel.visibility = View.GONE
            }
        }

        ibtnCancel.setOnClickListener {
            Log.d(TAG, "Cancel button clicked, clearing comment input")
            etComment.text.clear()
            ibtnCancel.visibility = View.GONE
        }
    }

    private fun setupCommentsSendingView(view: View) {
        etComment = view.findViewById(R.id.et_post_comment)
        ibtnSendComment = view.findViewById(R.id.ibtn_post_comment)

        val postId = arguments?.getString(ARG_POST_ID)

        ibtnSendComment.setOnClickListener {
            val commentText = etComment.text.toString().trim()
            if (commentText.isNotEmpty() && postId != null) {
                Log.d(TAG, "Sending comment: $commentText for post ID: $postId")
                sendComment(postId, commentText)
            } else {
                Log.w(TAG, "Comment is empty or post ID is null!")
                Toast("Comment cannot be empty", requireContext())
            }
        }
    }

    private fun sendComment(postId: String, commentText: String) {
        val firestore = FirebaseFirestore.getInstance()
        val commentsCollection = firestore.collection("posts").document(postId).collection("comments")

        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        val userProfile = sharedPreferencesHelper.getUserProfile()

        val comment = Comment(
            etComment.text.toString(),
            System.currentTimeMillis(),
            userProfile?.username
        )

        commentsCollection.add(comment)
            .addOnSuccessListener {
                Log.d(TAG, "Comment added successfully to post ID: $postId")
                etComment.text.clear()
                Toast("Comment added successfully", requireContext())
                fetchComments(postId) // Refresh the comments
                addOneToCommentCount(postId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to add comment: ${exception.message}")
                Toast("Failed to add comment", requireContext())
            }
    }

    private fun addOneToCommentCount(postId: String) {
        val db = FirebaseFirestore.getInstance().collection("posts").document(postId)

        // Use FieldValue.increment to increase the comment count by 1
        db.update("commentCount", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully incremented comment count for post: $postId")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to increment comment count: ${exception.message}")
            }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rv_post_comments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val postId = arguments?.getString(ARG_POST_ID)

        commentAdapter = CommentAdapter { commentId ->
            if (postId != null) {
                likeCommentInFirestore(commentId, postId)
            }
        }
        recyclerView.adapter = commentAdapter
        Log.d(TAG, "RecyclerView and adapter set up")
    }

    private fun likeCommentInFirestore(commentId: String, postId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val commentRef = firestore.collection("posts").document(postId).collection("comments").document(commentId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val newLikes = snapshot.getLong("likes")?.plus(1) ?: 1
            transaction.update(commentRef, "likes", newLikes)
            null
        }.addOnSuccessListener {
            Toast("Liked the comment!", requireContext())
        }.addOnFailureListener { exception ->
            Toast("Failed to like comment: ${exception.message}", requireContext())
        }
    }

    private fun fetchComments(postId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val commentsCollection = firestore.collection("posts").document(postId).collection("comments")

        Log.d(TAG, "Fetching comments for post ID: $postId")
        commentsCollection.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error fetching comments: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val commentsList = mutableListOf<Comment>()
                    for (doc in snapshot.documents) {
                        val comment = doc.toObject(Comment::class.java)
                        comment?.let {
                            it.commentId = doc.id
                            commentsList.add(it)
                        }
                    }
                    Log.d(TAG, "Fetched ${commentsList.size} comments for post ID: $postId")
                    commentAdapter.submitList(commentsList)
                } else {
                    Log.d(TAG, "No comments found for post ID: $postId")
                }
            }
    }

    private fun handleFetchError(exception: Exception) {
        Log.e(TAG, "Error fetching data: ${exception.message}")
        Toast("Failed to fetch data", requireContext())
    }
}
