package com.example.yourway.explore.postimagegrid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yourway.R

class ImagePagerAdapter(
    private val imageUrls: List<String>,
    private val post: Post, // Accept post object to send on image click
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.pagerImageView)


        init {
            itemView.setOnClickListener {
                listener.onImageClick(post) // Send the post on image click
                Log.d("ImageViewClicked", "clicked: ${imageUrls[adapterPosition]}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_explore_grid_box_item_imagepager, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(imageUrls[position])
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUrls.size


}
