package ru.pl.photogallery.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoResponse(
    @Json(name = "photo") val galleryItems: List<GalleryItem>,
    //added for paging library:
    @Json(name = "perpage") val perPage: Int,
    val pages: Int,
    val page: Int,
    val total: Int
)