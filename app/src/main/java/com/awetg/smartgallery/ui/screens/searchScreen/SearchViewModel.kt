package com.awetg.smartgallery.ui.screens.searchScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.domain.use_case.SearchUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchUseCases: SearchUseCases, private val sharedPreferenceUtil: SharedPreferenceUtil) :
    ViewModel() {
    var classificationUiState = mutableStateOf(ClassificationUiState())
        private set

    private var setClassificationJob: Job? = null

    init {
        updateClassificationState()
    }

    fun updateClassificationState() {
        setClassificationJob?.cancel()
        setClassificationJob = viewModelScope.launch {
            try {
                val mediaClassificationFlow = searchUseCases.getAllMediaClassification()
                val classifications = mediaClassificationFlow.first()
                classificationUiState.value = classificationUiState.value.copy(isLoading = false, classifications = classifications)
            } catch (ioe: IOException) {
                Log.e(LOG_TAG, "get classification exception: $ioe")
                classificationUiState.value = classificationUiState.value.copy(isLoading = false)
            }
        }
    }
}