package com.example.yourway.explore.postimagegrid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.yourway.R

class PostAdapter(
    private val posts: MutableList<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>(),OnImageClickListener {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.imageViewPager)
        private val singleImageView: ImageView = itemView.findViewById(R.id.singleImageView)
        private val ivItemType: ImageView = itemView.findViewById(R.id.iv_post_grid_item_type)

        fun bind(post: Post) {
            // If the post has multiple images, use ViewPager
            if (post.imageUrls.isNotEmpty()) {
                if (post.imageUrls.size > 1) {
                    viewPager.visibility = View.VISIBLE
                    singleImageView.visibility = View.GONE
                    val imageAdapter = ImagePagerAdapter(post.imageUrls, post, this@PostAdapter)
                    viewPager.adapter = imageAdapter

                    Glide.with(itemView.context)
                        .load(R.drawable.baseline_filter_none_24)
                        .into(ivItemType)
                    ivItemType.visibility = View.VISIBLE



                } else {
                    viewPager.visibility = View.GONE
                    singleImageView.visibility = View.VISIBLE
                    Glide.with(singleImageView.context)
                        .load(post.imageUrls[0])
                        .into(singleImageView)
                    Log.d("ImageURL", "Loading image from URL: ${post.imageUrls[0]}")
                }
            } else {
                // Handle the case where there are no images
                viewPager.visibility = View.GONE
                singleImageView.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onPostClick(post) // Trigger when clicked
                Log.d("ImageViewClicked","Item clicked")
            }
            viewPager.setOnClickListener {
                onPostClick(post) // Trigger when clicked
                Log.d("ImageViewClicked","view pager clicked")
            }

            itemView.findViewById<FrameLayout>(R.id.wrapperforvp2).setOnClickListener {
                onPostClick(post) // Trigger when clicked
                Log.d("ImageViewClicked"," wrapper clicked")

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_explore_grid_box_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun addPosts(newPosts: List<Post>) {
        for (post in newPosts) {
            if (!posts.contains(post)) {
                posts.add(post)
            }
        }
        notifyDataSetChanged()
    }

    override fun onImageClick(post: Post) {
        onPostClick(post) // Call the onPostClick method with the clicked post
    }

    fun refreshPosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }


}
