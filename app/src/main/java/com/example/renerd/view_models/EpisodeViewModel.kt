package com.example.renerd.view_models

import android.os.Parcel
import android.os.Parcelable
import com.example.renerd.core.database.Episode

data class EpisodeViewModel(
    val id: Long = 0L,
    val publishedAt: String = "",
    val duration: Long = 0L,
    val title: String = "",
    val slug: String = "",
    val episode: String = "",
    val product: String = "",
    val productName: String = "",
    val subject: String = "",
    val imageUrl: String? = null,
    val audioUrl: String = "",
    val description: String = "",
    val jumpToTime: Long = 0L,
    val guests: String = "",
    val postTypeClass: String = "",
    val elapsedTime: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(publishedAt)
        parcel.writeLong(duration)
        parcel.writeString(title)
        parcel.writeString(slug)
        parcel.writeString(episode)
        parcel.writeString(product)
        parcel.writeString(productName)
        parcel.writeString(subject)
        parcel.writeString(imageUrl)
        parcel.writeString(audioUrl)
        parcel.writeString(description)
        parcel.writeLong(jumpToTime)
        parcel.writeString(guests)
        parcel.writeString(postTypeClass)
        parcel.writeLong(elapsedTime)
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
        id = this.id,
        publishedAt = this.published_at,
        duration = this.duration,
        title = this.title,
        slug = this.slug,
        episode = this.episode,
        product = this.product,
        productName = this.product_name,
        subject = this.subject,
        imageUrl = this.image_url,
        audioUrl = this.audio_url,
        description = this.description,
        jumpToTime = this.jump_to_time,
        guests = this.guests,
        postTypeClass = this.post_type_class,
        elapsedTime = this.elapsed_time
    )
}
