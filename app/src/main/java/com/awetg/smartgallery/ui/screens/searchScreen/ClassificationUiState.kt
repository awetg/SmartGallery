package com.awetg.smartgallery.ui.screens.searchScreen

import com.awetg.smartgallery.data.entities.MediaClassification

data class ClassificationUiState(
    val isLoading: Boolean = true,
    val classifications: List<MediaClassification> = emptyList(),
)
