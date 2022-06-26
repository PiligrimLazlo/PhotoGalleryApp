package ru.pl.photogallery.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class PhotoRepository {

    private val flickrApi: FlickrApi

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        flickrApi = retrofit.create()
    }

    suspend fun fetchPhotos(): List<GalleryItem> {
        return flickrApi.fetchPhotos().photos.galleryItems
    }

}