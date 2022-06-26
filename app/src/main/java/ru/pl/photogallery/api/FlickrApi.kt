package ru.pl.photogallery.api

import retrofit2.http.GET

private const val API_KEY = "4232ef7bd2e329b0aa7443d604ecd87b"

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList" +
            "&api_key=$API_KEY" +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s")
    suspend fun fetchPhotos(): FlickrResponse
}