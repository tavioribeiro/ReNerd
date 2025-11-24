package com.podcast.renerd.core.network.model

import com.google.gson.annotations.SerializedName


data class EpisodeModel(
    val id: Int = 0,
    val url: String = "",
    @SerializedName("published_at") val publishedAt: String = "",
    @SerializedName("pub_date") val pubDate: String = "",
    @SerializedName("modified_at") val modifiedAt: String = "",
    val duration: Int = 0,
    val title: String = "",
    val slug: String = "",
    val episode: String = "",
    val product: String = "",
    @SerializedName("product_name") val productName: String = "",
    @SerializedName("product_email") val productEmail: String = "",
    @SerializedName("product_status") val productStatus: Boolean = false,
    @SerializedName("friendly_post_type") val friendlyPostType: String = "",
    @SerializedName("friendly_post_type_slug") val friendlyPostTypeSlug: String = "",
    @SerializedName("friendly_post_time") val friendlyPostTime: String = "",
    @SerializedName("friendly_post_date") val friendlyPostDate: String = "",
    val subject: String = "",
    val image: String = "",
    @SerializedName("image_alt") val imageAlt: String? = null,
    val thumbnails: Thumbnails = Thumbnails(),
    @SerializedName("audio_high") val audioHigh: String = "",
    @SerializedName("audio_medium") val audioMedium: String = "",
    @SerializedName("audio_low") val audioLow: String = "",
    @SerializedName("audio_zip") val audioZip: String = "",
    val insertions: List<Any> = emptyList(),
    val description: String = "",
    @SerializedName("jump-to-time") val jumpToTime: JumpToTime = JumpToTime(),
    val guests: String = "",
    @SerializedName("post_type_class") val postTypeClass: String = "",
    val category: Any = Any()
)

data class Thumbnails(
    @SerializedName("1536x1536") val x1536: String = "",
    @SerializedName("2048x2048") val x2048: String = "",
    @SerializedName("img-4x3-355x266") val img4x3355x266: String = "",
    @SerializedName("img-16x9-1210x544") val img16x91210x544: String = "",
    @SerializedName("img-16x9-760x428") val img16x9760x428: String = "",
    @SerializedName("img-4x6-448x644") val img4x6448x644: String = "",
    @SerializedName("img-1x1-3000x3000") val img1x13000x3000: String = ""
)

data class JumpToTime(
    val test: String = "",
    @SerializedName("start-time") val startTime: Int = 0,
    @SerializedName("end-time") val endTime: Int = 0
)

data class Category(
    @SerializedName("term_id") val termId: Int = 0,
    val name: String = "",
    val slug: String = "",
    @SerializedName("term_group") val termGroup: Int = 0,
    @SerializedName("term_taxonomy_id") val termTaxonomyId: Int = 0,
    val taxonomy: String = "",
    val description: String = "",
    val parent: Int = 0,
    val count: Int = 0,
    val filter: String = ""
)


/*
data class PodcastModel(
    val id: Int,
    val url: String,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("pub_date") val pubDate: String,
    @SerializedName("modified_at") val modifiedAt: String,
    val duration: Int,
    val title: String,
    val slug: String,
    val episode: String,
    val product: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_email") val productEmail: String,
    @SerializedName("product_status") val productStatus: Boolean,
    @SerializedName("friendly_post_type") val friendlyPostType: String,
    @SerializedName("friendly_post_type_slug") val friendlyPostTypeSlug: String,
    @SerializedName("friendly_post_time") val friendlyPostTime: String,
    @SerializedName("friendly_post_date") val friendlyPostDate: String,
    val subject: String,
    val image: String,
    @SerializedName("image_alt") val imageAlt: String?,
    val thumbnails: Thumbnails,
    @SerializedName("audio_high") val audioHigh: String,
    @SerializedName("audio_medium") val audioMedium: String,
    @SerializedName("audio_low") val audioLow: String,
    @SerializedName("audio_zip") val audioZip: String,
    val insertions: List<Insertion>,
    val description: String,
    @SerializedName("jump-to-time") val jumpToTime: JumpToTime,
    val guests: String,
    @SerializedName("post_type_class") val postTypeClass: String,
    val category: Any
)

data class Thumbnails(
    @SerializedName("1536x1536") val thumbnail1536: String,
    @SerializedName("2048x2048") val thumbnail2048: String,
    @SerializedName("img-4x3-355x266") val thumbnail4x3: String,
    @SerializedName("img-16x9-1210x544") val thumbnail16x9_1210: String,
    @SerializedName("img-16x9-760x428") val thumbnail16x9_760: String,
    @SerializedName("img-4x6-448x644") val thumbnail4x6: String,
    @SerializedName("img-1x1-3000x3000") val thumbnail1x1: String
)

data class Insertion(
    val id: Int,
    val title: String,
    val image: String,
    val link: String,
    @SerializedName("button-title") val buttonTitle: String,
    @SerializedName("start-time") val startTime: Int,
    @SerializedName("end-time") val endTime: Int,
    val sound: Boolean
)

data class JumpToTime(
    val test: String,
    @SerializedName("start-time") val startTime: Int,
    @SerializedName("end-time") val endTime: Int
)

data class Category(
    @SerializedName("term_id") val termId: Int,
    val name: String,
    val slug: String,
    @SerializedName("term_group") val termGroup: Int,
    @SerializedName("term_taxonomy_id") val termTaxonomyId: Int,
    val taxonomy: String,
    val description: String,
    val parent: Int,
    val count: Int,
    val filter: String
)*/