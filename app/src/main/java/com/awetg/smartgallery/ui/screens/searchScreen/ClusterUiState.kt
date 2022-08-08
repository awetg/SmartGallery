package com.awetg.smartgallery.ui.screens.searchScreen

import com.awetg.smartgallery.data.entities.MediaItem

data class ClusterUiState(
    val isLoading: Boolean = true,
    val clusters: List<List<MediaItem>> = emptyList(),
)
