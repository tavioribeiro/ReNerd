package com.example.renerd.view_models

import android.os.Parcel
import android.os.Parcelable
import com.example.renerd.core.database.Episode

data class EpisodeViewModel(
    var id: Int = 0,
    val publishedAt: String = "",
    var duration: Int = 0,
    var title: String = "",
    val slug: String = "",
    val episode: String = "",
    val product: String = "",
    var productName: String = "",
    val subject: String = "",
    var imageUrl: String? = null,
    var audioUrl: String = "",
    val description: String = "",
    val jumpToTime: Int = 0,
    val guests: String = "",
    val postTypeClass: String = "",
    var elapsedTime: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(publishedAt)
        parcel.writeInt(duration)
        parcel.writeString(title)
        parcel.writeString(slug)
        parcel.writeString(episode)
        parcel.writeString(product)
        parcel.writeString(productName)
        parcel.writeString(subject)
        parcel.writeString(imageUrl)
        parcel.writeString(audioUrl)
        parcel.writeString(description)
        parcel.writeInt(jumpToTime)
        parcel.writeString(guests)
        parcel.writeString(postTypeClass)
        parcel.writeInt(elapsedTime)
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

fun Episode.toEpisodeViewModel(): EpisodeViewModel {
    return EpisodeViewModel(
        id = this.id.toInt(),
        publishedAt = this.published_at,
        duration = this.duration.toInt(),
        title = this.title,
        slug = this.slug,
        episode = this.episode,
        product = this.product,
        productName = this.product_name,
        subject = this.subject,
        imageUrl = this.image_url,
        audioUrl = this.audio_url,
        description = this.description,
        jumpToTime = this.jump_to_time.toInt(),
        guests = this.guests,
        postTypeClass = this.post_type_class,
        elapsedTime = this.elapsed_time.toInt(),
    )
}
