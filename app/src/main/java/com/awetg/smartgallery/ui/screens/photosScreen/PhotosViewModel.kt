package com.awetg.smartgallery.ui.screens.photosScreen

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.use_case.PhotosUseCases
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
    private var getMediaAlbumsJob: Job? = null

    init {
        getMediaItems()
    }

    private fun getMediaItems() {
        getMediaItemsJob?.cancel()
        getMediaItemsJob = viewModelScope.launch {
            try {
                val mediaItemsFlow = photosUseCases.getMediaItems()
                val mediaItems = mediaItemsFlow.first()
//                    val mediaItems = mediaItemsFlow.flatMapConcat { it.asFlow() }.toList()
                photosUiState.value =
                    photosUiState.value.copy(mediaItems = mediaItems, isLoading = false)
                groupMediaItemByPath()
            } catch (ioe: IOException) {
                Log.e("smartImagesWorker", "get media items exception: $ioe")
            }
        }
    }

    private fun groupMediaItemByPath() {
        getMediaAlbumsJob?.cancel()
        getMediaAlbumsJob = viewModelScope.launch {
            try {
                val albums =
                    photosUiState.value.mediaItems.groupBy { it.parentPath }.values.toList()
                albumUiState.value = albumUiState.value.copy(isLoading = false, albums = albums)
            } catch (e: Exception) {
                Log.e("smartImagesWorker", "group media items exception: $e")
            }
        }
    }

    fun reloadMediaItems() {
        getMediaItems()
    }

    fun getNextMediaItemUri(mediaIndex: Int, albumIndex: Int): MediaItem? {
        if (photosUiState.value.mediaItems.isEmpty()) return null
        return when {
            albumIndex < 0 -> photosUiState.value.mediaItems.elementAt(mediaIndex)
            albumIndex >= 0 -> albumUiState.value.albums.elementAt(albumIndex).elementAt(mediaIndex)
            else -> null
        }
    }

    fun getMediaCount(albumIndex: Int): Int {
        if (photosUiState.value.mediaItems.isEmpty()) return 0
        return when {
            albumIndex < 0 -> photosUiState.value.mediaItems.count()
            albumIndex >= 0 -> albumUiState.value.albums.elementAt(albumIndex).count()
            else -> 0
        }
    }

    fun addItems(mediaItems: ArrayList<MediaItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            photosUseCases.addMediaItems(mediaItems).also { getMediaItems() }
        }
    }

    fun deleteAllMediaItems() {
        viewModelScope.launch(Dispatchers.IO) {
            photosUseCases.deleteAllMediaItems()
        }
    }

    private fun runFuncIfJobNotRunning(job: Job?, func: () -> Unit) {
        if (job == null || !job?.isActive!!) func()
    }
}