package com.example.yourway.forum

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class ForumDetailFragment : Fragment() {

    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var forumId: String

    // Views for displaying forum details
    private lateinit var imgUserProfile: ImageView
    private lateinit var txtUsername: TextView
    private lateinit var txtForumTitle: TextView
    private lateinit var txtForumDescription: TextView
    private lateinit var txtCreatedAt: TextView
    private lateinit var editTextComment: EditText
    private lateinit var btnSendComment: ImageButton

    // Upvote/Downvote Views
    private lateinit var btnUpvote: ImageButton
    private lateinit var btnDownvote: ImageButton
    private lateinit var txtUpvoteCount: TextView
    private lateinit var txtDownvoteCount: TextView

    // SwipeRefreshLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var upvoteLinearLayout: LinearLayout
    private lateinit var downvoteLinearLayout: LinearLayout


    private var upvoted: Boolean = false
    private var downvoted: Boolean = false
    private var upvoteCount: Int = 0
    private var downvoteCount: Int = 0

    companion object {
        fun newInstance(forumId: String): ForumDetailFragment {
            val fragment = ForumDetailFragment()
            val args = Bundle()
            args.putString("forumId", forumId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forum_detail, container, false)

        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout_forum_detail)
        forumRecyclerView = view.findViewById(R.id.rv_forum_comments)
        imgUserProfile = view.findViewById(R.id.iv_forumdetail_profile)
        txtUsername = view.findViewById(R.id.tv_forumdetail_username)
        txtForumDescription = view.findViewById(R.id.tv_forumdetail_description)

        txtCreatedAt = view.findViewById(R.id.tv_forumdetail_createdat)
        editTextComment = view.findViewById(R.id.et_forumdetail_main_comment)
        btnSendComment = view.findViewById(R.id.btn_forumdetail_main_send)

        txtForumTitle = view.findViewById(R.id.tv_forumdetail_forum_title)

        upvoteLinearLayout = view.findViewById(R.id.ll_forumDetail_upvote)
        downvoteLinearLayout = view.findViewById(R.id.ll_forumDetail_downvote)



        // Upvote/Downvote buttons
        btnUpvote = view.findViewById(R.id.btn_forumdetail_upvote)
        btnDownvote = view.findViewById(R.id.btn_forumdetail_downvote)
        txtUpvoteCount = view.findViewById(R.id.tv_forumdetail_upvote)
        txtDownvoteCount = view.findViewById(R.id.tv_forumdetail_downvote)

        // Set RecyclerView
        forumRecyclerView.layoutManager = LinearLayoutManager(context)

        commentAdapter = CommentAdapter(commentList, { replyText, parentId ->
            sendReplyToFirestore(replyText, parentId)
        }, { commentId ->
            likeCommentInFirestore(commentId)
        })

        forumRecyclerView.adapter = commentAdapter

        // Retrieve the forumId from arguments
        forumId = arguments?.getString("forumId").toString()
        if (forumId.isNotEmpty()) {
            fetchForumDetails()
            fetchComments()
            fetchVoteStatistics() // Fetch initial vote statistics
        } else {
            Toast("Forum ID not found", requireContext())
        }

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener {
            fetchComments()
            fetchVoteStatistics()
            swipeRefresh.isRefreshing = false // Stop refreshing
        }

        // Handle send comment button click
        btnSendComment.setOnClickListener {
            val commentText = editTextComment.text.toString()
            if (commentText.isNotEmpty()) {
                sendComment(commentText)
            } else {
                Toast("Please enter a comment", requireContext())
            }
        }

        // Upvote button click
        upvoteLinearLayout.setOnClickListener {
            if (!upvoted) {
                upvoted = true
                upvoteCount++
                updateVoteStatistics()
            }
        }

        // Downvote button click
        downvoteLinearLayout.setOnClickListener {
            if (!downvoted) {
                downvoted = true
                downvoteCount++
                updateVoteStatistics()
            }
        }

        return view
    }

    private fun fetchForumDetails() {
        val db = FirebaseFirestore.getInstance()
        db.collection("forums").document(forumId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("createdBy")
                    txtUsername.text = username


                    if (username != null) {
                        getImageUrlFromUsername(username) { imageSrc ->
                            if (imageSrc.isNotEmpty()) {
                                // Use Glide to load the image into an ImageView
                                Glide.with(this)
                                    .load(imageSrc)
                                    .placeholder(R.drawable.placeholder_image)
                                    .circleCrop()
                                    .error(R.drawable.placeholder_image)
                                    .into(imgUserProfile)
                            } else {
                                Toast("No image found", requireContext())
                            }
                        }
                    }
                    txtForumTitle.text = document.getString("title")
                    txtForumDescription.text = document.getString("description")

                    // Check if 'createdAt' is a timestamp and convert it to string
                    val createdAt = document.getLong("createdAt")
                    txtCreatedAt.text = createdAt?.let {
                        // Format date if needed, using SimpleDateFormat for custom formatting
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(it)
                    } ?: "Unknown Date" // Fallback if createdAt is null
                }
            }
    }

    private fun getImageUrlFromUsername(username: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(username)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imageSrc = document.getString("imageSrc") ?: ""
                    callback(imageSrc) // Pass the result to the callback
                } else {
                    callback("") // Return empty if no document found
                }
            }
            .addOnFailureListener {
                callback("") // Handle failure by returning an empty string
            }
    }

    //    private fun fetchComments() {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("forums").document(forumId).collection("comments")
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//            .get()
//            .addOnSuccessListener { documents ->
//                commentList.clear()
//                for (document in documents) {
//                    val comment = document.toObject(Comment::class.java)
//                    comment.id = document.id
//                    commentList.add(comment)
//                }
//                commentAdapter.notifyDataSetChanged()
//            }
//    }
    private fun fetchComments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("forums").document(forumId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // Clear existing comments
                commentList.clear()
                val commentMap = mutableMapOf<String, Comment>() // Map to store comments by ID

                // First pass to collect all comments
                for (document in documents) {
                    val comment = document.toObject(Comment::class.java)
                    comment.id = document.id
                    commentMap[comment.id] = comment // Store comment by ID
                }

                // Second pass to organize comments and replies
                for (comment in commentMap.values) {
                    if (comment.parentId == null) {
                        // Top-level comment, add to the main list
                        commentList.add(comment)
                    } else {
                        // It's a reply, find its parent and add it to the parent's replies list
                        commentMap[comment.parentId]?.let { parentComment ->
                            parentComment.replies.add(comment) // Assuming `replies` is a MutableList<Comment> in Comment class
                        }
                    }
                }

                // Notify adapter of data changes
                commentAdapter.notifyDataSetChanged()
            }
    }


    private fun fetchVoteStatistics() {
        val db = FirebaseFirestore.getInstance()
        db.collection("forums").document(forumId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    upvoteCount = document.getLong("upvotes")?.toInt() ?: 0
                    downvoteCount = document.getLong("downvotes")?.toInt() ?: 0
                    txtUpvoteCount.text = upvoteCount.toString()
                    txtDownvoteCount.text = downvoteCount.toString()
                }
            }
    }

    private fun sendComment(commentText: String) {
        val db = FirebaseFirestore.getInstance()
        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        val userProfile = sharedPreferencesHelper.getUserProfile()
        val username = userProfile?.username

        val comment = Comment(
            commentText,
            System.currentTimeMillis(),
            username.toString()
        ) // Add the appropriate username
        db.collection("forums").document(forumId).collection("comments").add(comment)
            .addOnSuccessListener {
                editTextComment.text.clear()
                fetchComments() // Refresh comments
            }
    }

    private fun updateVoteStatistics() {
        val db = FirebaseFirestore.getInstance()
        val updates = hashMapOf<String, Any>(
            "upvotes" to upvoteCount,
            "downvotes" to downvoteCount
        )
        db.collection("forums").document(forumId).update(updates)
            .addOnSuccessListener {
                txtUpvoteCount.text = upvoteCount.toString()
                txtDownvoteCount.text = downvoteCount.toString()
            }
    }

    private fun sendReplyToFirestore(replyText: String, parentId: String) {
        val db = FirebaseFirestore.getInstance()
        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        val userProfile = sharedPreferencesHelper.getUserProfile()

        val username = userProfile?.username
        val reply = hashMapOf(
            "content" to replyText,
            "parentId" to parentId,
            "timestamp" to System.currentTimeMillis(),
            "username" to username // Replace with actual username
        )

        db.collection("forums")
            .document(forumId)
            .collection("comments")
            .add(reply)
            .addOnSuccessListener {
                fetchComments() // Refresh comments after reply is sent
            }
            .addOnFailureListener {
                Toast("Reply Failed to Register", requireContext())
            }
    }

    private fun likeCommentInFirestore(commentId: String) {
        val db = FirebaseFirestore.getInstance()
        val commentRef = db.collection("forums")
            .document(forumId)
            .collection("comments")
            .document(commentId)

        // Atomically increment the like count
        db.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(commentRef, "likes", currentLikes + 1)
        }.addOnSuccessListener {
            fetchComments() // Refresh comments after the like
        }.addOnFailureListener {
            Toast("Like Failed to Register", requireContext())
        }
    }
}
