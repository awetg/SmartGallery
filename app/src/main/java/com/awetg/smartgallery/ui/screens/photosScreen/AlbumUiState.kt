package com.awetg.smartgallery.ui.screens.photosScreen

import com.awetg.smartgallery.data.entities.MediaItem

data class AlbumUiState(
    val isLoading: Boolean = true,
    val albums: List<List<MediaItem>> = emptyList(),
)
