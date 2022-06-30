package ru.pl.photogallery.api

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.pl.photogallery.utils.Constants

private const val TAG = "PagingSource"

class PagingSource(
    private val flickrApi: FlickrApi,
    private val query: String
) : PagingSource<Int, GalleryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        return try {
            val currentPage = params.key ?: 1

            val currentPhotos: List<GalleryItem> = if (query.isEmpty()) {
                val interestingPhotosResponse =
                    flickrApi.fetchPhotos(currentPage, params.loadSize).photos
                getCurrentListPhotos(currentPage, interestingPhotosResponse)
            } else {
                val searchResponse =
                    flickrApi.searchPhotos(query, currentPage, params.loadSize).photos
                getCurrentListPhotos(currentPage, searchResponse)
            }

            val prev = if (currentPage > 1) currentPage - 1 else null
            val next = if (currentPhotos.isEmpty()) null else currentPage + 1
            Log.d(TAG, "prev: $prev, current: $currentPage next: $next, query: $query")
            LoadResult.Page(
                data = currentPhotos,
                prevKey = prev,
                nextKey = next
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private fun getCurrentListPhotos(currentPage: Int, response: PhotoResponse): List<GalleryItem> {
        val shouldNotLoadMore = currentPage > response.pages
        return if (shouldNotLoadMore) emptyList()
        else response.galleryItems
    }
}