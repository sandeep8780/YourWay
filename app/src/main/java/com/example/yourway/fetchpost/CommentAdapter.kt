package com.example.yourway.fetchpost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.Toast
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private val onLikeComment: (String) -> Unit
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)

    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentTextView: TextView = itemView.findViewById(R.id.tv_postcomment_content)
        private val likesTextView: TextView = itemView.findViewById(R.id.tv_postcomment_like_count)
        private val btnCommentLike: ImageButton = itemView.findViewById(R.id.btn_postcomment_like)
        private val usernameTextView: TextView = itemView.findViewById(R.id.tv_postcomment_username)

        fun bind(comment: Comment) {
            contentTextView.text = comment.commentText
            likesTextView.text = "Likes: ${comment.likes}"
            btnCommentLike.setOnClickListener {
                onLikeComment(comment.commentId)
            }
            usernameTextView.text = comment.username
        }
    }


}

class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.commentId == newItem.commentId
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}
