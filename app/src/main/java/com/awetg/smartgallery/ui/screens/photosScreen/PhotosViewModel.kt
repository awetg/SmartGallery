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
class PhotosViewModel @Inject constructor(private val photosUseCases: PhotosUseCases, private val sharedPreferenceUtil: SharedPreferenceUtil) :
    ViewModel() {

    var photosUiState = mutableStateOf(PhotosUiState())
        private set

    var albumUiState = mutableStateOf(AlbumUiState())
        private set

    var clusterUiState = mutableStateOf(ClusterUiState())
        private set

    var mlJobState = mutableStateOf(MLJobState())
        private set

    private var getMediaItemsJob: Job? = null
    private var getMediaAlbumsJob: Job? = null
    private var getMediaClusterJob: Job? = null

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
                updateMLJobState()
                groupMediaItemByClusterId()
            } catch (ioe: IOException) {
                Log.e(LOG_TAG, "get media items exception: $ioe")
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
                Log.e(LOG_TAG, "group media items by parent path exception: $e")
            }
        }
    }

    fun reloadMediaItems() {
        getMediaItems()
    }

    fun getNextMediaItemUri(mediaIndex: Int, groupIndex: Int, groupType: String): MediaItem? {
        if (photosUiState.value.mediaItems.isEmpty()) return null
        return when(groupType) {
            ALBUM_GROUP -> {
                return when {
                    groupIndex < 0 -> photosUiState.value.mediaItems.elementAt(mediaIndex)
                    groupIndex >= 0 -> albumUiState.value.albums.elementAt(groupIndex).elementAt(mediaIndex)
                    else -> null
                }
            }
            CLUSTER_GROUP -> {
                return if (groupIndex >= 0) clusterUiState.value.clusters.elementAt(groupIndex).elementAt(mediaIndex) else null
            }
            else -> null
        }
    }

    fun getMediaCount(groupIndex: Int, groupType: String): Int {
        if (photosUiState.value.mediaItems.isEmpty()) return 0
        return when(groupType) {
            ALBUM_GROUP -> {
                return when {
                    groupIndex < 0 -> photosUiState.value.mediaItems.count()
                    groupIndex >= 0 -> albumUiState.value.albums.elementAt(groupIndex).count()
                    else -> 0
                }
            }
            CLUSTER_GROUP -> {
                return if (groupIndex >= 0) clusterUiState.value.clusters.elementAt(groupIndex).count() else 0
            }
            else -> 0
        }
    }

    private fun groupMediaItemByClusterId() {
        if (mlJobState.value.clusterJobComplete) {
            getMediaClusterJob?.cancel()
            getMediaClusterJob = viewModelScope.launch {
                try {
                    val clusters =
                        photosUiState.value.mediaItems.filter { it.clusterId > -1 }.groupBy { it.clusterId }.values.toList()
                    clusterUiState.value = clusterUiState.value.copy(isLoading = false, clusters = clusters)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "group media items by clusterId exception: $e")
                }
            }
        } else {
            Log.d(LOG_TAG, "cluster job not complete")
        }
    }

    fun updateMLJobState() {
        val clusterJobComplete = sharedPreferenceUtil.prefs.getBoolean(SharedPreferenceUtil.CLUSTER_JOB_COMPLETE, false)
        mlJobState.value = mlJobState.value.copy(clusterJobComplete = clusterJobComplete)
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