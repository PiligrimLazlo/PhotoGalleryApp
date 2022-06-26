package ru.pl.photogallery.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GalleryItem(
    val title: String,
    val id: String,
    @Json(name = "url_s") val url: String, //имя в JSON файле НЕ совпадает с полем
)
