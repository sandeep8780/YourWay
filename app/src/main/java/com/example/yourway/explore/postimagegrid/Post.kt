package com.example.yourway.explore.postimagegrid

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Post(
    val description: String = "",
    val imageUrls: List<String> = listOf(),
    val timestamp: Timestamp? = null,
    val username: String = "",
    var id: String = ""
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeStringList(imageUrls)
        parcel.writeParcelable(timestamp, flags)
        parcel.writeString(username)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post = Post(parcel)
        override fun newArray(size: Int): Array<Post?> = arrayOfNulls(size)
    }
}



