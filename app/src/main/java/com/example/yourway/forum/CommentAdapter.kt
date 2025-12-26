package com.example.yourway.forum

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R

class CommentAdapter(
    private val commentList: List<Comment>,
    private val onReplySend: (String, String) -> Unit, // Callback for sending replies
    private val onLikeComment: (String) -> Unit // Callback for liking the comment
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.tv_comment_content)
        val likeCount: TextView = itemView.findViewById(R.id.tv_comment_like_count)
        val replyButton: ImageButton = itemView.findViewById(R.id.tv_comment_reply_button)
        val likeButton: ImageButton = itemView.findViewById(R.id.btn_comment_like)
        val replyLayout: ConstraintLayout = itemView.findViewById(R.id.tv_comment_reply_layout)
        val replyInput: EditText = itemView.findViewById(R.id.et_reply_input)
        val sendReplyButton: Button = itemView.findViewById(R.id.btn_send_reply)
        val btnCancel: ImageButton = itemView.findViewById(R.id.ibtn_comment_reply_cancel)
        val tvUsername: TextView = itemView.findViewById(R.id.tv_comment_username)
        val viewlastline: View = itemView.findViewById(R.id.view_last_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        holder.content.text = comment.content
        holder.likeCount.text = comment.likes.toString() // Display the number of likes
        holder.tvUsername.text = comment.username

        // Check if this comment is a reply
        val isReply = comment.parentId != null
        holder.itemView.setPadding(if (isReply) 60 else 0, holder.itemView.paddingTop, holder.itemView.paddingRight, holder.itemView.paddingBottom)

        // Toggle reply layout visibility
        holder.replyButton.setOnClickListener {
            holder.replyLayout.visibility = if (holder.replyLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Handle sending the reply
        holder.sendReplyButton.setOnClickListener {
            val replyText = holder.replyInput.text.toString().trim()
            if (replyText.isNotEmpty()) {
                onReplySend(replyText, comment.id) // Send the reply

                // Clear input and hide the reply layout after sending
                holder.replyInput.text.clear()
                holder.replyLayout.visibility = View.GONE
            }
        }

        // Handle like button click
        holder.likeButton.setOnClickListener {
            onLikeComment(comment.id) // Trigger the like event
        }

        holder.replyInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                holder.btnCancel.visibility = if (s.toString().isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.btnCancel.setOnClickListener {
            holder.btnCancel.visibility = View.GONE
            holder.replyInput.text.clear()
            holder.replyLayout.visibility = View.GONE
        }

        // Clear previous replies from the parent view
        holder.itemView.findViewById<ViewGroup>(R.id.replies_container).removeAllViews()

        // Display replies if they exist
        if (comment.replies.isNotEmpty()) {
            for (reply in comment.replies) {
                // Inflate reply view
                val replyView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.forum_comment_item, holder.itemView.findViewById(R.id.replies_container), false)

                val replyHolder = CommentViewHolder(replyView)
                replyHolder.content.text = reply.content
                replyHolder.tvUsername.text = reply.username
                replyHolder.likeCount.text = reply.likes.toString()

                replyHolder.replyButton.visibility = View.GONE
                replyHolder.viewlastline.visibility = View.GONE

                // Handle like button click for replies
                replyHolder.likeButton.setOnClickListener {
                    onLikeComment(reply.id) // Trigger the like event for the reply
                }



                // Adjust padding for replies
                replyView.setPadding(60, 0, 0, 0) // Add padding to indicate this is a reply

                // Add the reply view to the replies container
                holder.itemView.findViewById<ViewGroup>(R.id.replies_container).addView(replyView)
            }
        }
    }

    override fun getItemCount() = commentList.size
}
