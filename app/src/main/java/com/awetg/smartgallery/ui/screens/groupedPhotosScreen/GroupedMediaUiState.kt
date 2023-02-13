package com.awetg.smartgallery.ui.screens.groupedPhotosScreen

import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.data.entities.MediaItem

data class GroupedMediaUiState(
    val isLoading: Boolean = false,
    val mediaItems: List<MediaItem> = emptyList(),
)
