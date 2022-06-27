package ru.pl.photogallery.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import ru.pl.photogallery.utils.Constants.PHOTO_GALLERY_ITEM_PER_PAGE

private const val TAG = "PhotoRepository"

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

    //For Paging library
    suspend fun fetchPhotos(page: Int): List<GalleryItem> {
        val photosResponse = flickrApi.fetchPhotos(page, PHOTO_GALLERY_ITEM_PER_PAGE).photos
        return if (page * PHOTO_GALLERY_ITEM_PER_PAGE > photosResponse.total) {
            emptyList()
        } else {
            photosResponse.galleryItems
        }
    }

}