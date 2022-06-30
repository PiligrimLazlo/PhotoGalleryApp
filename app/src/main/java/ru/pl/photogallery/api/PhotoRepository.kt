package ru.pl.photogallery.api

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
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

    //For Paging library
    fun fetchPhotos(perPage: Int): Flow<PagingData<GalleryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false
            ), pagingSourceFactory = { PagingSource(flickrApi, "") }).flow
    }

    suspend fun searchPhotos(query: String): List<GalleryItem> {
        return flickrApi.searchPhotos(query).photos.galleryItems
    }

    //For Paging library
    fun searchPhotos(query: String, perPage: Int): Flow<PagingData<GalleryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false
            ), pagingSourceFactory = { PagingSource(flickrApi, query) }).flow
    }

}