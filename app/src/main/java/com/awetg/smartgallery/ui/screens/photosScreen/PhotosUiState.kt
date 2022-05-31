package com.awetg.smartgallery.ui.screens.photosScreen

import com.awetg.smartgallery.data.entities.MediaItem

data class PhotosUiState(
    val isLoading: Boolean = true,
    val mediaItems: List<MediaItem> = emptyList(),
)