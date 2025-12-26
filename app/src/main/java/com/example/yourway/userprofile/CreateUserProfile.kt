package com.example.yourway.userprofile

import com.example.yourway.Toast
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.yourway.BaseActivity
import com.example.yourway.R
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.UUID

class CreateUserProfile : Fragment() {

    private lateinit var btnEditProfileUsername: Button
    private lateinit var popupWindow: PopupWindow

    private lateinit var tvProfileUsername: TextView
    private lateinit var etProfileDisplayName: EditText
    private lateinit var etProfileBio: EditText
    private lateinit var etProfileLink: EditText
    private lateinit var btnProfileUpdateProfile: Button
    private lateinit var btnfinish : Button

    private lateinit var btnProfileSelectImage: Button
    private var imageUri: Uri? = null
    private lateinit var selectedImagePath: String

    private lateinit var ivProfileImage: ImageView
    private lateinit var username: String
    private lateinit var email: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the email from the arguments
        username = arguments?.getString("email").toString()
        email = username
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_create_user_profile, container, false)

        btnEditProfileUsername = view.findViewById(R.id.btn_profile_edit_username)
        tvProfileUsername = view.findViewById(R.id.tv_profile_username)
        etProfileDisplayName = view.findViewById(R.id.et_profile_display_name)
        etProfileBio = view.findViewById(R.id.et_profile_bio)
        etProfileLink = view.findViewById(R.id.et_profile_link)
        btnProfileSelectImage = view.findViewById(R.id.btn_profile_select_profile_image)
        ivProfileImage = view.findViewById(R.id.iv_profile_image)
        btnProfileUpdateProfile = view.findViewById(R.id.btn_profile_update_profile)
        btnfinish = view.findViewById(R.id.btn_profile_finish)

        btnEditProfileUsername.setOnClickListener {
            showUsernameUpdatePopupWindow()
        }

        btnfinish.setOnClickListener {
            val intent = Intent(activity,BaseActivity::class.java)
            requireContext().startActivity(intent)
            requireActivity().finish()
        }

        btnProfileSelectImage.setOnClickListener {
            val intent = ImagePicker.with(requireActivity())
                .provider(ImageProvider.GALLERY)
                .setMultipleAllowed(false)
                .crop()
                .cropSquare()
                .galleryMimeTypes(
                    mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg")
                )
                .createIntent()
            imagePickerLauncher.launch(intent)

        }

        btnProfileUpdateProfile.setOnClickListener {
            val username = tvProfileUsername.text.toString()
            val displayName = etProfileDisplayName.text.toString()
            val bio = etProfileBio.text.toString()
            val link = etProfileLink.text.toString()
            val fileName = UUID.randomUUID().toString()

            setUsernameToEmail(email,username)
            if (imageUri != null) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child("profile_images/$fileName")
                val uploadTask = storageRef.putFile(imageUri!!)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            Log.e("ImageUploadError", it.message!!)
//                        com.example.yourway.Toast.makeText(requireContext(), "Image upload failed", com.example.yourway.Toast.LENGTH_SHORT).show()
                        }
                        return@continueWithTask null
                    }
                    storageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { downloadUri ->
                            val imageUrl = downloadUri.toString()
                            // Proceed with updating the profile in Firestore
                            updateProfileInFirestore(username, displayName, bio, link, email, imageUrl)
//                        com.example.yourway.Toast.makeText(requireContext(), "Profile updated successfully", com.example.yourway.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("DownloadUrlError", task.exception?.message ?: "Unknown error")
//                    com.example.yourway.Toast.makeText(requireContext(), "Failed to get download URL", com.example.yourway.Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Log.e("UploadFailure", it.message ?: "Unknown error")
//                com.example.yourway.Toast.makeText(requireContext(), "Upload failed", com.example.yourway.Toast.LENGTH_SHORT).show()
                }
            } else {
                updateProfileInFirestore(username, displayName, bio, link, email)
            }
        }

        tvProfileUsername.text = username
        loadUserProfileBasicData(username) { userProfileBasicData ->
            if (userProfileBasicData != null) {
                Toast(username,requireContext())
                tvProfileUsername.text = username
                etProfileDisplayName.setText(userProfileBasicData.displayName)
                etProfileBio.setText(userProfileBasicData.bio)
                etProfileLink.setText(userProfileBasicData.link)

                loadProfileImageView(userProfileBasicData.imageSrc)

            } else {
//                Toast("Unable to Load Profile...", requireContext())
                val db = FirebaseFirestore.getInstance()

                // Assuming you have access to the user's email
                val userEmail = username // Change this to the actual email variable if different

                // Create a document in the "Users" collection with the email as the document ID
                val userProfileData = hashMapOf(
                    "email" to userEmail,
                    // Add any other default fields you want to initialize
                )

                db.collection("users")
                    .document(userEmail) // Use email as the document ID
                    .set(userProfileData)
                    .addOnSuccessListener {
                        setUsernameToEmail(email,username)
                        Toast("Profile Initialized for $userEmail", requireContext())
                    }
                    .addOnFailureListener { e ->
                        Toast("Error creating profile: ${e.message}", requireContext())
                        e.message?.let { Log.d("ProfileError", it) }
                    }
            }
        }

        //set image picker


        return view
    }

    private fun setUsernameToEmail(email: String,username: String) {
        val db = FirebaseFirestore.getInstance()

        // Reference to the "usernametoemail" collection
        val emailRef = db.collection("usernametoemail").document(email)

        // Create a map to hold the email data
        val emailData = hashMapOf(
            "username" to username
        )

        // Add the document to Firestore
        emailRef.set(emailData)
            .addOnSuccessListener {
                println("Document added with email: $email")
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    private fun updateProfileInFirestore(
        username: String,
        displayName: String,
        bio: String,
        link: String,
        email:String
    ) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val userDoc = usersCollection.document(username)

        val userData = hashMapOf(
            "displayName" to displayName,
            "bio" to bio,
            "link" to link,
            "email" to email
        )
        userDoc.update(userData as Map<String, Any>)
            .addOnSuccessListener {
                setUsernameToEmail(email,username)
                saveUsernameToPreferences(username)
                Toast("Profile updated successfully", requireContext())
            }
            .addOnFailureListener { e ->
                Log.e("UserProfileUpdateError", e.message!!)
                Toast("Error updating profile", requireContext())
            }
    }

    private fun updateProfileInFirestore(
        username: String,
        displayName: String,
        bio: String,
        link: String,
        email: String,
        imageUrl: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val userDoc = usersCollection.document(username)

        val userData = hashMapOf(
            "displayName" to displayName,
            "bio" to bio,
            "link" to link,
            "email" to email,
            "imageSrc" to imageUrl
        )

        userDoc.set(userData)
            .addOnSuccessListener {
                setUsernameToEmail(email,username)
                saveUsernameToPreferences(username)
                Toast("Profile updated successfully", requireContext())
                btnfinish.visibility = View.VISIBLE

            }
            .addOnFailureListener { e ->
                Log.e("UserProfileUpdateError", e.message!!)
                Toast("Error updating profile", requireContext())
            }
    }

    private fun saveUsernameToPreferences(username: String) {
        val sharedPref = requireContext().getSharedPreferences("UserPref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("username", username)

        editor.apply() // Save changes asynchronously
    }

    private fun loadProfileImageView(imageSrc: String?) {
        val task = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<String, Void, Bitmap>() {
            override fun doInBackground(vararg params: String): Bitmap? {
                var bitmap: Bitmap? = null
                try {
                    val url = URL(params[0])
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return bitmap
            }

            override fun onPostExecute(bitmap: Bitmap?) {
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap)
                }
            }
        }
        task.execute(imageSrc)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            // Handle successful image selection
            val uri = res.data?.data
            uri?.let { imageUri = it }
            if (uri != null) {
                processImage(uri)
                Toast("Image Selected", requireContext())
            }
        } else if (res.resultCode == ImagePicker.RESULT_ERROR) {
            // Handle errors more gracefully
            val error = ImagePicker.getError(res.data)
            Toast("Image picker Error : $error", requireContext())
        } else if (res.resultCode == Activity.RESULT_CANCELED) {
            Toast("ImagePicker Canceled", requireContext())
        }
    }

    // Process the selected image and update the adapter
    private fun processImage(imageUri: Uri) {
        val filePath = getRealPathFromURI(imageUri)
        if (filePath != null) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            ivProfileImage.setImageBitmap(bitmap) // Set the decoded bitmap to the ImageView
        } else {
            Log.e("ImagePickerFragment", "Failed to get the file path from URI: $imageUri")
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

    private fun loadUserProfileBasicData(
        username: String,
        callback: (UserProfileBasicData?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(username).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val userData = document.data
                    val userProfileBasicData = UserProfileBasicData(
                        displayName = userData?.get("displayName") as? String,
                        bio = userData?.get("bio") as? String,
                        link = userData?.get("link") as? String,
                        email = userData?.get("email") as? String,
                        imageSrc = userData?.get("imageSrc") as? String
                    )
                    callback(userProfileBasicData)
                } else {
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }

    private fun showUsernameUpdatePopupWindow() {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_username_update, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.animationStyle = R.style.PopupAnimation

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        val etPopupUsername = popupView.findViewById<EditText>(R.id.et_popup_username)
        val btnPopupCheck = popupView.findViewById<Button>(R.id.btn_popup_check)
        val btnPopupSaveChanges = popupView.findViewById<Button>(R.id.btn_popup_save_changes)
        val tvPopupError = popupView.findViewById<TextView>(R.id.tv_popup_error)
        val tvPopupCancel = popupView.findViewById<TextView>(R.id.tv_popup_cancel)

        btnPopupSaveChanges.isEnabled = false
        var isUsernameUpdated = false
        popupWindow.setOnDismissListener {
            if (isUsernameUpdated) {
                Toast("Username Updated", requireContext())
            } else if (etPopupUsername.text.isNotEmpty()) {
                Toast("Changes Discarded", requireContext())
            }
        }
        tvPopupCancel.setOnClickListener {
            popupWindow.dismiss()
        }
        btnPopupCheck.setOnClickListener {
            Toast("Checking For username !!!", requireContext())
            val username = etPopupUsername.text.toString()
            if (username.isNotEmpty()) {
                checkIfUsernameExists(username) { exists ->
                    if (!exists) {
                        tvPopupError.text = "Valid Username... All Good to Go..!!!"
                        tvPopupError.isVisible = true
                        btnPopupSaveChanges.isEnabled = true
                    } else {
                        tvPopupError.text = "Username Exists... Try Other..."
                        tvPopupError.isVisible = true
                        btnPopupSaveChanges.isEnabled = false
                    }
                }
            } else {
                tvPopupError.text = "Username cannot be Empty"
                tvPopupError.isVisible = true
                btnPopupSaveChanges.isEnabled = false
            }
        }

        btnPopupSaveChanges.setOnClickListener {
            val newUsername = etPopupUsername.text.toString()
            val oldUsername = tvProfileUsername.text.toString()

            if (newUsername.isNotEmpty()) {
                checkIfUsernameExists(newUsername) { exists ->
                    if (exists) {
                        tvPopupError.text = "Username Exists... Try Other..."
                        tvPopupError.isVisible = true
                        btnPopupSaveChanges.isEnabled = false
                    } else {
                        updateUsername(oldUsername, newUsername)
                        isUsernameUpdated = true
                        tvProfileUsername.text = newUsername
                        popupWindow.dismiss()
                    }
                }
            }
        }
    }

    //vertical realign with keyboard popup
//    override fun onResume() {
//        super.onResume()
//        val view = view
//        view?.viewTreeObserver?.addOnGlobalLayoutListener {
//            val rect = Rect()
//            view.getWindowVisibleDisplayFrame(rect)
//            val screenHeight = view.rootView.height
//            val keyboardHeight = screenHeight - rect.bottom
//            if (keyboardHeight > screenHeight * 0.15) {
//                // Keyboard is visible, adjust the popup window's position
//                popupWindow.update(0, 0, popupWindow.width, popupWindow.height)
//            }
//        }
//    }

    private fun updateUsername(oldUsername: String, newUsername: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(oldUsername).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val data = document.data
                    usersCollection.document(oldUsername).delete()
                    usersCollection.document(newUsername).set(data!!)
                } else {
                    Toast("Data not Found", requireContext())
                }
            } else {
                Toast("Task Failed", requireContext())
            }
        }
    }

    private fun checkIfUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")
        userCollection.document(username).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    callback(true)
                } else {
                    callback(false)
                }
            } else {
                Toast("Error", requireContext())
                callback(false)
            }
        }
    }


}