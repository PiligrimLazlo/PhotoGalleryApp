package ru.pl.photogallery.api

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
private const val TAG = "PagingSource"

class PagingSource(
    private val repository: PhotoRepository
) : PagingSource<Int, GalleryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        return try {
            val currentPage = params.key ?: 1
            val fetchPhotos: List<GalleryItem> =
                repository.fetchPhotos(currentPage)

            val prev = if (currentPage > 1) currentPage - 1 else null
            val next = if (fetchPhotos.isEmpty()) null else currentPage + 1
            Log.d(TAG, "prev: $prev, current: $currentPage next: $next")
            LoadResult.Page(
                data = fetchPhotos,
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
}