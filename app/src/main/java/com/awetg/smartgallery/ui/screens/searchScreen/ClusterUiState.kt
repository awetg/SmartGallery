package com.awetg.smartgallery.ui.screens.searchScreen

import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.data.entities.MediaItem

data class ClusterUiState(
    val isLoading: Boolean = false,
    val classifications: List<MediaClassification> = emptyList(),
)
