package com.example.yourway.post

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.databinding.FragmentBasicPostBinding
import com.google.firebase.firestore.FirebaseFirestore

import androidx.activity.enableEdgeToEdge


class BasicPost : Fragment() {

    // Declare the binding variable
    private var _binding: FragmentBasicPostBinding? = null
    private val binding get() = _binding!!

    private val post = Post() // This will hold the post data including title, description, and images

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //load the images fragment initially
        val fragment = ImagePickerFragment { imageUris ->
            post.imageUrls = imageUris.map { it }.toMutableList()
            if (post.imageUrls.isNotEmpty()){
                binding.btnGotoPostReview.isEnabled = true
            }
            else{
                binding.btnGotoPostReview.isEnabled = false
            }
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fcv_create_post, fragment)
            .commit()

        binding.switchCreatepostImageVideo.visibility = View.GONE
        binding.tvChoiceImages.visibility = View.GONE
        binding.tvChoiceVideo.visibility = View.GONE


//        binding.switchCreatepostImageVideo.setOnCheckedChangeListener { _, isChecked ->
//            if(isChecked){
////                val fragment = VideoPicker { videoUri ->
////                    // Handle the selected video URI here
////                    post.videoUrl = videoUri.toString()  // Store the video URL in your post object
////                    // You can also update your UI or perform any further operations
////                }
//
////                childFragmentManager.beginTransaction()
////                    .replace(R.id.fcv_create_post, fragment)
////                    .commit()
//            }
//            else {
//                val fragment = ImagePickerFragment { imageUris ->
//                    post.imageUrls = imageUris.map { it }.toMutableList()
//                }
//                childFragmentManager.beginTransaction()
//                    .replace(R.id.fcv_create_post, fragment)
//                    .commit()
//            }
//        }


//        // Handle Image Picker button click
//        binding.btnDisplayImagePicker.setOnClickListener {
//            val fragment = ImagePickerFragment { imageUris ->
//                post.imageUrls = imageUris.map { it }.toMutableList()
//            }
//            childFragmentManager.beginTransaction()
//                .replace(R.id.image_picker_container, fragment)
//                .commit()
//        }

        // Handle Review Post button click
        binding.btnGotoPostReview.setOnClickListener {
            // Collect title and description
            post.title = binding.etPostTitle.text.toString()
            post.description = binding.etPostDescription.text.toString()

            // Open PostReviewFragment and pass the post data
            val fragment = PostReview.newInstance(post)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcv_addpost, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}