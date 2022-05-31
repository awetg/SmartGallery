package com.awetg.smartgallery.ui.screens.photosScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.awetg.smartgallery.ui.components.ThumbnailGridViewer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosScreen(onImageClickNavigation: (String) -> Unit, viewModel: PhotosViewModel = hiltViewModel()) {
    val state = viewModel.photosUiState.value
    ThumbnailGridViewer(mediaItems = state.mediaItems, onImageClickNavigation, -1)
}

