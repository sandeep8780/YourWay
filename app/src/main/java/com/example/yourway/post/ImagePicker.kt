package com.example.yourway.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.databinding.FragmentImagePickerBinding
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ImagePickerFragment(private val onImagesPicked: (MutableList<Uri>) -> Unit) : Fragment() {
    private lateinit var binding: FragmentImagePickerBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var selectedImagePaths = mutableListOf<String>()
    private var uriList: MutableList<Uri> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerViewImages
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        adapter = ImageAdapter(selectedImagePaths) { position ->
            // This block is executed when an item is removed
            uriList.removeAt(position) // Remove the image path from the fragment's list
            selectedImagePaths.removeAt(position)

            onImagesPicked(uriList)

        }
        recyclerView.adapter = adapter

        binding.btnSelectImage.setOnClickListener {

            val intent = ImagePicker.with(requireActivity())
                .provider(ImageProvider.GALLERY) // Allows both camera and gallery
                .crop()
                .cropFreeStyle()
                .galleryMimeTypes(
                    mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg")
                )

                .createIntent()
            imagePickerLauncher.launch(intent)


        }

        binding.btnSelectImages.setOnClickListener {

            val intent = ImagePicker.with(requireActivity())
                .provider(ImageProvider.GALLERY) // Allows both camera and gallery
                .setMultipleAllowed(true)
                .crop()
                .cropFreeStyle()
                .galleryMimeTypes(
                    mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg")
                )

                .createIntent()
            imagePickerLauncher.launch(intent)


        }

        binding.btnDone.visibility = View.GONE
//        binding.btnDone.setOnClickListener {
//            onImagesPicked(uriList)
//            parentFragmentManager.popBackStack()
//            Toast.makeText(
//                requireContext(),
//                "Image Selection Complete " + uriList.size,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }


    // Activity result launcher for image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {

            // Handle successful image selection
            val clipData = res.data?.clipData
            val data = res.data?.data
//            val uriList = mutableListOf<Uri>()

            uriList = res.data?.getParcelableArrayListExtra("extra.multiple_file_path") ?: mutableListOf()
            if (data != null){
                Toast.makeText(requireContext(), "data ......", Toast.LENGTH_SHORT).show()
                uriList.add(data)
                Log.d("imgpicker","single")
            }

            if (clipData != null) {
                Toast.makeText(requireContext(), "data ......", Toast.LENGTH_SHORT).show()
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                    Log.d("imgpicker","multiple")
                }
            }
//
            // Process and display the images
            processImages(uriList)
            Toast.makeText(
                requireContext(),
                "Process Image Completer ${res.data} " + clipData?.itemCount,
                Toast.LENGTH_SHORT
            ).show()
        } else if (res.resultCode == ImagePicker.RESULT_ERROR) {
            // Handle errors more gracefully
            val error = ImagePicker.getError(res.data)
            Toast.makeText(requireContext(), "Image Picker Error: $error", Toast.LENGTH_SHORT)
                .show()
        } else if (res.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireContext(), "Image Picker Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    // Process the selected images and update the adapter
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
            onImagesPicked(uriList)
            adapter.notifyDataSetChanged()
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

    // Adapter to display the images
    private class ImageAdapter(
        private val imagePaths: MutableList<String>,
        private val onItemRemoved: (Int) -> Unit // A callback function to notify the Fragment
    ) : RecyclerView.Adapter<ImageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imagePath = imagePaths[position]
            val bitmap = BitmapFactory.decodeFile(imagePath)

            holder.imageView.setImageBitmap(bitmap) // Set the decoded bitmap to the ImageView

            holder.imageView.setOnClickListener {
                // Remove item from the list and notify the adapter
//                imagePaths.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount) // To refresh the rest of the list

                // Notify the Fragment to remove the item from its scope
                onItemRemoved(position)
            }
        }

        override fun getItemCount(): Int {
            return imagePaths.size
        }
    }

    private class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }
}
