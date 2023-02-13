package com.awetg.smartgallery.ui.screens.searchScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.domain.use_case.SearchUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchUseCases: SearchUseCases) :
    ViewModel() {
    var objectDetectionUiState = mutableStateOf(ObjectDetectionUiState())
        private set
    var clusterUiState = mutableStateOf(ClusterUiState())
        private set

    private var setClassificationJob: Job? = null

    init {
        setClassificationJob?.cancel()
        setClassificationJob = viewModelScope.launch {
            setClassificationState()
        }
    }

    private suspend fun setClassificationState() {
        try {
            val mediaClassificationFlow = searchUseCases.getAllMediaClassification()
            val classifications = mediaClassificationFlow.first()
            if (classifications.isNotEmpty()) {
                val clusterClassifications = classifications.filter { it.type == MediaClassification.CLASSIFICATION_TYPE_CLUSTER }
                val objectDetectionClassifications = classifications
                    .filter { it.type == MediaClassification.CLASSIFICATION_TYPE_OBJECT_DETECTION }
                    .sortedBy { it.name }
                objectDetectionUiState.value = objectDetectionUiState.value.copy(isLoading = false, classifications = objectDetectionClassifications)
                clusterUiState.value = clusterUiState.value.copy(isLoading = false, classifications = clusterClassifications)
            }
        } catch (ioe: IOException) {
            Log.e(LOG_TAG, "get classification exception: $ioe")
            objectDetectionUiState.value = objectDetectionUiState.value.copy(isLoading = false)
        }
    }

    fun updateClassificationState() {
        setClassificationJob?.cancel()
        setClassificationJob = viewModelScope.launch {
            setClassificationState()
        }
    }
}