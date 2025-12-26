package com.example.yourway.post

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

@SuppressLint("ParcelCreator")
data class Post(
    var username : String = "",
    var title: String = "",
    var description: String = "",
    var imageUrls: MutableList<Uri> = mutableListOf(),
    var videoUrl: String = "",
    val commentCount: Int = 0,
    val likes: Int = 0

) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title);
        dest.writeString(description);
    }
}