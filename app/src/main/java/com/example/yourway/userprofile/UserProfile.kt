package com.example.yourway.userprofile
import android.os.Parcel
import android.os.Parcelable

data class UserProfile(
    val username: String?= "",
    val displayName: String?= "",
    val bio: String? = "",
    val link: String? = "",
    val imgSrc: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(displayName)
        parcel.writeString(bio)
        parcel.writeString(link)
        parcel.writeString(imgSrc)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserProfile> {
        override fun createFromParcel(parcel: Parcel): UserProfile {
            return UserProfile(parcel)
        }

        override fun newArray(size: Int): Array<UserProfile?> {
            return arrayOfNulls(size)
        }
    }
}
