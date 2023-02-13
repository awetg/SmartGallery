package com.awetg.smartgallery.ui.screens.groupedPhotosScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.use_case.SearchUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GroupedMediaViewModel @Inject constructor(private val searchUseCases: SearchUseCases) :
    ViewModel() {
    var groupedMediaUiState = mutableStateOf(GroupedMediaUiState())
        private set

    suspend fun setGroupedMedia(mediaItems:  List<MediaItem>) {
        withContext(Dispatchers.IO) {
            try {
                groupedMediaUiState.value = groupedMediaUiState.value.copy(isLoading = false, mediaItems = mediaItems)
            } catch (ioe: IOException) {
                Log.e(LOG_TAG, "error setting grouped media: $ioe")
                groupedMediaUiState.value = groupedMediaUiState.value.copy(isLoading = false)
            }
        }
    }

    suspend fun setGroupedMediaFromClassification(classificationId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val mediaClassificationFlow = searchUseCases.getAllMediaClassificationByIdUseCase(classificationId)
                val classification = mediaClassificationFlow.first()
                val mediaItemsFlow = searchUseCases.getMediaItemsByIds(classification.mediaItemIds)
                val mediaItems = mediaItemsFlow.first()
                groupedMediaUiState.value = groupedMediaUiState.value.copy(isLoading = false, mediaItems = mediaItems)
            } catch (ioe: IOException) {
                Log.e(LOG_TAG, "error setting grouped media: $ioe")
                groupedMediaUiState.value = groupedMediaUiState.value.copy(isLoading = false)
            }
        }
    }
}