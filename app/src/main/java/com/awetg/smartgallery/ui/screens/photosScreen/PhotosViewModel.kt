package com.awetg.smartgallery.ui.screens.photosScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awetg.smartgallery.common.ALBUM_GROUP
import com.awetg.smartgallery.common.CLUSTER_GROUP
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.use_case.PhotosUseCases
import com.awetg.smartgallery.ui.screens.libraryScreen.AlbumUiState
import com.awetg.smartgallery.ui.screens.searchScreen.ClusterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(private val photosUseCases: PhotosUseCases) :
    ViewModel() {

    var photosUiState = mutableStateOf(PhotosUiState())
        private set

    var albumUiState = mutableStateOf(AlbumUiState())
        private set

    private var getMediaItemsJob: Job? = null

    init {
        getMediaItemsByModifiedAt()
    }

    fun getMediaItemsByModifiedAt() {
        getMediaItemsJob?.cancel()
        getMediaItemsJob = viewModelScope.launch {
            try {
                val mediaItemsFlow = photosUseCases.getMediaItemsByModifiedAt()
                val mediaItems = mediaItemsFlow.first()
                photosUiState.value = photosUiState.value.copy(mediaItems = mediaItems, isLoading = false)
                val albums = mediaItems.groupBy { it.parentPath }.values.toList()
                albumUiState.value = albumUiState.value.copy(isLoading = false, albums = albums)
            } catch (ioe: IOException) {
                Log.e(LOG_TAG, "get media items exception: $ioe")
            }
        }
    }

}