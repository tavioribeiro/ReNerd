package com.example.renerd.view_models

import android.os.Parcel
import android.os.Parcelable




data class EpisodeViewModel(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val audioUrl: String = "",
    val duration: Int = 0,
    val publishedAt: String = "",
    val category: Any = Any()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeString(audioUrl)
        parcel.writeInt(duration)
        parcel.writeString(publishedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EpisodeViewModel> {
        override fun createFromParcel(parcel: Parcel): EpisodeViewModel {
            return EpisodeViewModel(parcel)
        }

        override fun newArray(size: Int): Array<EpisodeViewModel?> {
            return arrayOfNulls(size)
        }
    }
}