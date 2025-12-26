package com.example.yourway.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.google.api.Distribution.BucketOptions.Linear
import org.w3c.dom.Text

class ForumAdapter(private val forumList: List<Forum>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<ForumAdapter.ForumViewHolder>() {

    inner class ForumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainLinearLayout: LinearLayout = itemView.findViewById(R.id.ll_forum_main)
        val title: TextView = itemView.findViewById(R.id.tv_forumlist_title)
        val description: TextView = itemView.findViewById(R.id.tv_forumlist_description)
        val upvoteCount: TextView = itemView.findViewById(R.id.tv_forumlist_upvote_count)
        val downvoteCount: TextView = itemView.findViewById(R.id.tv_forumlist_downvote_count)
        val expandButton: ImageButton = itemView.findViewById(R.id.btn_forumlist_expand_button)
        val createBy: TextView = itemView.findViewById(R.id.tv_forumlist_createby)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_list_item, parent, false)
        return ForumViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        val forum = forumList[position]
        holder.title.text = forum.title
        holder.description.text = forum.description
        holder.upvoteCount.text = forum.upvotes.toString()
        holder.downvoteCount.text = forum.downvotes.toString()
        holder.createBy.text = forum.createdBy

        holder.expandButton.setOnClickListener {
            onItemClick(forum.id)
        }
        holder.mainLinearLayout.setOnClickListener {
            onItemClick(forum.id)
        }

    }

    override fun getItemCount() = forumList.size
}
