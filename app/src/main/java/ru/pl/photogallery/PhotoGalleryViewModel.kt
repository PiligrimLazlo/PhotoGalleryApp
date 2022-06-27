package ru.pl.photogallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import ru.pl.photogallery.api.GalleryItem
import ru.pl.photogallery.api.PagingSource
import ru.pl.photogallery.api.PhotoRepository
import ru.pl.photogallery.utils.Constants.PHOTO_GALLERY_ITEM_PER_PAGE

private const val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel : ViewModel() {
    private val photoRepository = PhotoRepository()

//    private val _galleryItems: MutableStateFlow<List<GalleryItem>> = MutableStateFlow(emptyList())
//    val galleryItems: StateFlow<List<GalleryItem>> = _galleryItems.asStateFlow()

    //Paging library
    val galleryItems: Flow<PagingData<GalleryItem>> = Pager(
        PagingConfig(
            pageSize = PHOTO_GALLERY_ITEM_PER_PAGE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { PagingSource(photoRepository) }
    ).flow.cachedIn(viewModelScope)

    /*init {
        viewModelScope.launch {
            try {
                val items = photoRepository.fetchPhotos()
                Log.d(TAG, "Items received: $items")
                _galleryItems.value = items
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch gallery items", ex)
            }
        }
    }*/

}