package ru.pl.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.pl.photogallery.api.GalleryItem
import ru.pl.photogallery.api.PhotoRepository
import ru.pl.photogallery.utils.Constants.PHOTO_GALLERY_ITEM_PER_PAGE

private const val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel : ViewModel() {
    private val photoRepository = PhotoRepository()
    private val preferencesRepository = PreferencesRepository.get()

    private val _uiState: MutableStateFlow<PhotoGalleryUiState> =
        MutableStateFlow(PhotoGalleryUiState())
    val uiState: StateFlow<PhotoGalleryUiState>
        get() = _uiState.asStateFlow()

    //Paging library
    val galleryItems: Flow<PagingData<GalleryItem>>

    init {
        viewModelScope.launch {
            preferencesRepository.storedQuery.collectLatest { storedQuery ->
                try {
                    //val items = fetchGalleryItems(storedQuery)

                    _uiState.update { oldState ->
                        oldState.copy(/*images = items,*/ query = storedQuery)
                    }

                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to fetch gallery items", ex)
                }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isPolling.collect { isPolling ->
                _uiState.update { it.copy(isPolling = isPolling) }
            }
        }

        //paging
        /*viewModelScope.launch {
            _uiState.collectLatest {
                galleryItems = fetchGalleryItems(it.query, PHOTO_GALLERY_ITEM_PER_PAGE)
                    .cachedIn(viewModelScope)
            }
        }*/

        galleryItems = uiState
            .flatMapLatest { fetchGalleryItems(query = it.query, PHOTO_GALLERY_ITEM_PER_PAGE) }
            .cachedIn(viewModelScope)


    }

    fun setQuery(query: String) {
        viewModelScope.launch {
            preferencesRepository.setStoredQuery(query)
        }
    }

    fun toggleIsPolling() {
        viewModelScope.launch {
            preferencesRepository.setPolling(!uiState.value.isPolling)
        }
    }

    private suspend fun fetchGalleryItems(query: String): List<GalleryItem> {
        return if (query.isNotEmpty()) {
            photoRepository.searchPhotos(query)
        } else {
            photoRepository.fetchPhotos()
        }
    }


    //paging
    private fun fetchGalleryItems(query: String, perPage: Int): Flow<PagingData<GalleryItem>> {
        return if (query.isNotEmpty()) {
            photoRepository.searchPhotos(query, perPage)
        } else {
            photoRepository.fetchPhotos(perPage)
        }
    }

}

data class PhotoGalleryUiState(
    //val images: List<GalleryItem> = listOf(),
    val query: String = "",
    val isPolling: Boolean = false
)