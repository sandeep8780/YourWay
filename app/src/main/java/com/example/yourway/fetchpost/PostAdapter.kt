package com.example.yourway.fetchpost

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.yourway.BaseActivity
import com.example.yourway.Toast
import com.example.yourway.explore.DisplayOtherUserProfile
import com.example.yourway.forum.ForumAdapter
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class PostAdapter(
    private val postList:List<Post>,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val onItemClick: (String) -> Unit
): RecyclerView.Adapter<PostAdapter.PostViewHolder>(){

    private val TAG = "PostAdapter"

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvUsername: TextView = itemView.findViewById(R.id.tv_post_username)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_post_description)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_post_title)
        val ivProfileImage: ImageView = itemView.findViewById(R.id.iv_post_userProfile)
        val vpImages: ViewPager2 = itemView.findViewById(R.id.vp_post)
        val tvlikesCount:TextView = itemView.findViewById(R.id.tv_post_likes)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
        val btnShowComment: ImageButton = itemView.findViewById(R.id.ibtn_post_show_comment)
        val btnPostLike: ImageButton = itemView.findViewById(R.id.ibtn_post_like)
        val ibtnMoreMenu: ImageButton = itemView.findViewById(R.id.ibtn_post_moremenu)

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostAdapter.PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent,false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = postList.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PostAdapter.PostViewHolder, position: Int) {
        val post = postList[position]
        holder.tvUsername.text = post.username
        holder.tvTitle.text = post.title
        holder.tvDescription.text = post.description
        holder.tvlikesCount.text = post.likes.toString()
        holder.tvCommentCount.text = post.commentCount.toString()

        holder.tvUsername.setOnClickListener {
            val clickedUsername = holder.tvUsername.text.toString()

            val currentUsername = sharedPreferencesHelper.getUserProfile()?.username

            if (clickedUsername != currentUsername) {
                // If the clicked username is different from the current username
                val fragmentTransaction = (holder.itemView.context as BaseActivity).supportFragmentManager.beginTransaction()
                val fragment = DisplayOtherUserProfile()

                // Pass the username to the fragment (you can use a Bundle)
                val bundle = Bundle()
                bundle.putString("username", clickedUsername)
                fragment.arguments = bundle

                // Replace the existing fragment in the fcv_base
                fragmentTransaction.replace(R.id.fcv_base, fragment)
                fragmentTransaction.commit()
            } else {
                // If the clicked username is the same as the current username, navigate to the User profile
                val bottomNav = (holder.itemView.context as BaseActivity).findViewById<BottomNavigationView>(R.id.bottom_nav)
                bottomNav.selectedItemId = R.id.profile // Set the ID of the user profile navigation item
            }
        }

        holder.ivProfileImage.setOnClickListener {
            val clickedUsername = holder.tvUsername.text.toString()

            val currentUsername = sharedPreferencesHelper.getUserProfile()?.username

            if (clickedUsername != currentUsername) {
                // If the clicked username is different from the current username
                val fragmentTransaction = (holder.itemView.context as BaseActivity).supportFragmentManager.beginTransaction()
                val fragment = DisplayOtherUserProfile()

                // Pass the username to the fragment (you can use a Bundle)
                val bundle = Bundle()
                bundle.putString("username", clickedUsername)
                fragment.arguments = bundle

                // Replace the existing fragment in the fcv_base
                fragmentTransaction.replace(R.id.fcv_base, fragment)
                fragmentTransaction.commit()
            } else {
                // If the clicked username is the same as the current username, navigate to the User profile
                val bottomNav = (holder.itemView.context as BaseActivity).findViewById<BottomNavigationView>(R.id.bottom_nav)
                bottomNav.selectedItemId = R.id.profile // Set the ID of the user profile navigation item
            }
        }


        val username = sharedPreferencesHelper.getUserProfile()?.username
        if (username == post.username){
            holder.ibtnMoreMenu.visibility = View.VISIBLE
        }
        else{
            holder.ibtnMoreMenu.visibility = View.GONE
        }

        //double tap like
        // Gesture detector for double tap
        val gestureDetector = GestureDetector(holder.itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                likePost(post.postId, holder)
                return true
            }
        })

        // Set touch listener to detect double-tap on the image or description
        holder.vpImages.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }




        holder.ibtnMoreMenu.setOnClickListener {
            // Inflate the menu and create a PopupMenu
            val popupMenu = PopupMenu(holder.itemView.context, holder.ibtnMoreMenu)
            popupMenu.menuInflater.inflate(R.menu.post_more_menu, popupMenu.menu)

            popupMenu.setForceShowIcon(true)

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.more_menu_edit -> {
                        // Handle edit post action
                        onEditPost(post,holder)
                        true
                    }
                    R.id.more_menu_delete -> {
                        // Handle delete post action
                        onDeletePost(post,holder)
                        true
                    }
                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }

        // Add real-time listeners for likes and comments
        observePostChanges(post.postId, holder)

        holder.btnShowComment.setOnClickListener {
            Toast("hello , {${post.postId}}",holder.itemView.context)
            //now i want to call the comments bottom sheet fragment here
            val commentsBottomSheet = CommentBottomSheetFragment.newInstance(post.postId)
            commentsBottomSheet.show((holder.itemView.context as FragmentActivity).supportFragmentManager, "CommentsBottomSheet")

        }

        holder.btnPostLike.setOnClickListener {
            likePost(post.postId,holder)
        }



        loadUserProfileImage(post.username,holder.ivProfileImage)
        val imageAdapter = ImagePagerAdapter(post.imageUrls){
            likePost(post.postId,holder)
        }
        holder.vpImages.adapter = imageAdapter


    }

    private fun likePost(postId: String, holder: PostAdapter.PostViewHolder) {
        // Check if the button is already in the "liked" state
        if (holder.btnPostLike.tag != "liked") {
            val db = FirebaseFirestore.getInstance().collection("posts").document(postId)

            // Increment the "likes" field in Firestore
            db.update("likes", FieldValue.increment(1))
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully incremented like count for post: ${postId}")

                    // Change the button icon to the "liked" state
                    holder.btnPostLike.setImageResource(R.drawable.baseline_thumb_up_alt_24)
                    holder.btnPostLike.tag = "liked" // Set the tag to "liked"
                    Toast("Post liked!", holder.itemView.context)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error incrementing like count: ${exception.message}")
                    Toast("Failed to like post", holder.itemView.context)
                }
        }
    }

    // Add methods to handle edit and delete actions
    private fun onEditPost(post: Post, holder: PostViewHolder) {
        Log.d(TAG, "Edit post clicked for post ID: ${post.postId}")

        // Create a new instance of EditPostBottomSheetFragment with the postId
        val editPostBottomSheet = EditPostBottomSheetFragment.newInstance(post)

        editPostBottomSheet.show((holder.itemView.context as FragmentActivity).supportFragmentManager, "CommentsBottomSheet")
    }

    private fun onDeletePost(post: Post, holder: PostViewHolder) {
        // Log the post ID being deleted
        Log.d(TAG, "Delete post clicked for post ID: ${post.postId}")

        // Create an AlertDialog for confirmation
        val builder = AlertDialog.Builder(holder.itemView.context)
        builder.setTitle("Delete Post")
        builder.setMessage("Are you sure you want to delete this post?")

        // Positive button action
        builder.setPositiveButton("Yes") { dialog, _ ->
            // Call the delete function
            deletePost(post.postId)
            holder.itemView.visibility = View.GONE

            dialog.dismiss() // Dismiss the dialog
        }

        // Negative button action
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Just dismiss the dialog if "No" is clicked
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    // Function to delete the post document in Firestore
    private fun deletePost(postId: String) {
        val db = FirebaseFirestore.getInstance()

        // Delete the document with the specified postId
        db.collection("posts")
            .document(postId)
            .delete()
            .addOnSuccessListener {
                Log.e(TAG, "Post Delete Successfully")
            }
            .addOnFailureListener { e ->
                // Handle failure, e.g., log the error or show an error message
                Log.e(TAG, "Error deleting post: ", e)

            }
    }

    private fun observePostChanges(postId: String, holder: PostAdapter.PostViewHolder) {
        val db = FirebaseFirestore.getInstance().collection("posts").document(postId)

        db.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Listen failed: $exception")
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val newLikesCount = documentSnapshot.getLong("likes") ?: 0
                val newCommentCount = documentSnapshot.getLong("commentCount") ?: 0

                // Update UI with the new values
                holder.tvlikesCount.text = newLikesCount.toString()
                holder.tvCommentCount.text = newCommentCount.toString()
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun loadUserProfileImage(username: String, imageView: ImageView) {
        if (username.isEmpty()) {
            Log.w(TAG, "loadUserProfileImage: Username is empty")
            imageView.setImageResource(R.drawable.placeholder_image) // Set default image
            return
        }

        Log.d(TAG, "loadUserProfileImage: Loading image for username $username")
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(username)

        // Fetch the user's document
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val imageSrc = document.getString("imageSrc")
                Log.d(TAG, "loadUserProfileImage: Found image source $imageSrc for username $username")

                // Use Glide to load the image into the ImageView
                if (!imageSrc.isNullOrEmpty()) {
                    Glide.with(imageView.context)
                        .load(imageSrc)
                        .circleCrop()
                        .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                        .into(imageView)
                } else {
                    Log.w(TAG, "loadUserProfileImage: No image source found for username $username")
                    imageView.setImageResource(R.drawable.placeholder_image) // Default image
                }
            } else {
                Log.w(TAG, "loadUserProfileImage: Document does not exist for username $username")
                imageView.setImageResource(R.drawable.placeholder_image) // Default image
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "loadUserProfileImage: Error fetching document for username $username", exception)
            imageView.setImageResource(R.drawable.placeholder_image) // Set a default image in case of an error
        }
    }
}
