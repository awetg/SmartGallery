package com.awetg.smartgallery.ui.screens.photosScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.awetg.smartgallery.common.ALL_GROUP
import com.awetg.smartgallery.common.CLUSTER_GROUP
import com.awetg.smartgallery.ui.components.ThumbnailGridViewer
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.screens.groupedPhotosScreen.GroupedMediaViewModel
import kotlinx.coroutines.launch

@Composable
fun PhotosScreen(
    onImageClickNavigation: (String) -> Unit,
    photosViewModel: PhotosViewModel = hiltViewModel(),
    groupViewModel: GroupedMediaViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = 1) {
        coroutineScope.launch { groupViewModel.setGroupedMedia(photosViewModel.photosUiState.value.mediaItems) }
    }
    val state = groupViewModel.groupedMediaUiState.value
    if (state.mediaItems.isNotEmpty()) {
            ThumbnailGridViewer(state.mediaItems, onImageClickNavigation)
    }
}

