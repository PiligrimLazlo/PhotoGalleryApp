package ru.pl.photogallery.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import ru.pl.photogallery.utils.Constants.PHOTO_GALLERY_ITEM_PER_PAGE

private const val TAG = "PhotoRepository"

class PhotoRepository {

    private val flickrApi: FlickrApi

    init {
        val okhttpClient = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okhttpClient)
            .build()
        flickrApi = retrofit.create()
    }

    //simple loading a100 pictures
    suspend fun fetchPhotos(): List<GalleryItem> {
        return flickrApi.fetchPhotos().photos.galleryItems
    }

    //For Paging library todo should return Flow<PagingData<GalleryItem>>
    suspend fun fetchPhotos(page: Int): List<GalleryItem> {
        val photosResponse = flickrApi.fetchPhotos(page, PHOTO_GALLERY_ITEM_PER_PAGE).photos
        return if (page * PHOTO_GALLERY_ITEM_PER_PAGE > photosResponse.total) {
            emptyList()
        } else {
            photosResponse.galleryItems
        }
    }

    suspend fun searchPhotos(query: String): List<GalleryItem> {
        return flickrApi.searchPhotos(query).photos.galleryItems
    }

    //For Paging library todo should return Flow<PagingData<GalleryItem>>
     suspend fun searchPhotos(query: String, page: Int): List<GalleryItem> {
         return flickrApi.searchPhotos(query, page, PHOTO_GALLERY_ITEM_PER_PAGE).photos.galleryItems
     }

}