package com.example.yourway.post

import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.databinding.FragmentPostReviewBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PostReview : Fragment() {

    private lateinit var binding: FragmentPostReviewBinding
    private lateinit var post: Post
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var recyclerView: RecyclerView
    private var selectedImagePaths: MutableList<String> = mutableListOf()
    private var imageUris: MutableList<Uri> = mutableListOf()
    private val imageFirebaseStorageUris: MutableList<String> = mutableListOf()
    private lateinit var imageAdapter: ImageAdapter // Assuming you have an ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(post: Post): PostReview {
            val fragment = PostReview()
            val args = Bundle()
            args.putParcelable("post", post)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore and Firebase Storage instances
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()



        // Retrieve the Post object from arguments
        post = requireArguments().getParcelable("post")!!

        // Set up the RecyclerView to display images
        imageUris.addAll(post.imageUrls.map { it }) // Assuming `imageUrls` contains the URIs
        imageAdapter = ImageAdapter(imageUris)
        Toast("Load Review : " + imageUris[0],requireContext())

        recyclerView = binding.recyclerViewSelectedImages
        recyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        recyclerView.adapter = imageAdapter
        processImages(imageUris)
//        binding.recyclerViewSelectedImages.apply {
//            layoutManager = GridLayoutManager(requireContext(), 3)
//            adapter = imageAdapter
//        }


        // Display the post title and description
        binding.tvTitle.text = post.title
        binding.tvDescription.text = post.description

        binding.btnSubmitPost.setOnClickListener {
            // Upload logic to Firebase Storage and Firestore
            uploadImagesToStorageAndSavePost()

        }
    }

    private fun processImages(imageUris: List<Uri>?) {
        imageUris?.let {
            for (uri in imageUris) {
                val filePath = getRealPathFromURI(uri)
                if (filePath != null) {
                    selectedImagePaths.add(filePath)

                    Log.d("SelectedImage", "Image path: $filePath")
                } else {
                    Log.e("ImagePickerFragment", "Failed to get the file path from URI: $uri")
                }
            }
            imageAdapter.notifyDataSetChanged()
            // Notify the adapter that the data has changed
        }
    }

    // Get the real file path from the URI
    private fun getRealPathFromURI(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File.createTempFile("temp", ".jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
    }

    private fun uploadImagesToStorageAndSavePost() {
        val imageUrls = mutableListOf<String>()
        val postDoc = db.collection("posts").document()

        // Check if there are images to upload
        if (imageUris.isEmpty()) {
            savePostToFirestore(post, imageUrls)
        } else {
            // Upload images one by one to Firebase Storage
            for (uri in imageUris) {
                val fileName = UUID.randomUUID().toString()
                val storageRef = storage.reference.child("post_images/$fileName")

                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            imageUrls.add(downloadUri.toString())
                            // Check if all images have been uploaded
                            if (imageUrls.size == imageUris.size) {
                                savePostToFirestore(post, imageUrls)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast("Error uploading image: $e",requireContext())
                    }
            }
        }
    }

    private fun savePostToFirestore(
        post: Post, // Assuming you have a Post model class
        imageUrls: List<String>
    ) {
        val username = getUsernameFromPreferences()
        // Get a reference to the "posts" collection in Firestore
        val postCollection = FirebaseFirestore.getInstance().collection("posts")

        // Create a new document in the "posts" collection
        val newPostDoc = postCollection.document()

        // Set the post data with image references
        val postData = hashMapOf(
            "username" to getUsernameFromPreferences(),
            "title" to post.title,
            "description" to post.description,
            "imageUrls" to imageUrls, // Store the list of image URLs
            "timestamp" to FieldValue.serverTimestamp(), // Optionally add a timestamp
            "commentCount" to 0,
            "likes" to 0

        )

        // Upload the post data to Firestore
        newPostDoc.set(postData)
            .addOnSuccessListener {
                Toast( "Post uploaded successfully!",requireContext())
                requireActivity().finish()
                // Navigate back or perform any other action after upload
            }
            .addOnFailureListener { e ->
                Toast("Error uploading post: ${e.message}",requireContext())
            }
    }

    private fun getUsernameFromPreferences(): String {
        val sharedPref = requireActivity().getSharedPreferences("UserPref", MODE_PRIVATE)
        return sharedPref.getString("username", "") ?: "" // Return username or empty string if not found
    }


}

class ImageAdapter(private val imageUris: MutableList<Uri>) : RecyclerView.Adapter<ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageUris[position]
        Log.d("ImageAdapter", "Loading image from Uri: $uri")

        try {
            val inputStream = holder.itemView.context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap)
                Log.d("ImageAdapter", "Image loaded successfully")
            } else {
                Log.e("ImageAdapter", "Failed to load image from Uri: $uri")
            }
        } catch (e: Exception) {
            Log.e("ImageAdapter", "Error loading image from Uri: $uri", e)
        }
    }



    override fun getItemCount(): Int {
        return imageUris.size
    }
}



class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imageView: ImageView = view.findViewById(R.id.image_view)
}
