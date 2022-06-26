package ru.pl.photogallery.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FlickrResponse(
    val photos: PhotoResponse
)