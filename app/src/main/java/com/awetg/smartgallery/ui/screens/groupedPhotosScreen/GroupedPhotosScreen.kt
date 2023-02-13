package com.awetg.smartgallery.ui.screens.groupedPhotosScreen

import android.util.Log
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.ui.components.ThumbnailGridViewer
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import com.awetg.smartgallery.ui.screens.searchScreen.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupedPhotosScreen(
    groupId: Long,
    groupType: String,
    onBackNavigationClick: () -> Unit,
    onImageClickNavigation: (String) -> Unit,
    photosViewModel: PhotosViewModel = hiltViewModel(),
    groupViewModel: GroupedMediaViewModel = hiltViewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = groupId, key2 = groupType) {
        when(groupType) {
            ALL_GROUP -> groupViewModel.setGroupedMedia(photosViewModel.photosUiState.value.mediaItems)
            ALBUM_GROUP -> groupViewModel.setGroupedMedia(photosViewModel.albumUiState.value.albums.elementAt(groupId.toInt()))
            CLUSTER_GROUP, OBJECT_DETECTION_GROUP -> coroutineScope.launch {groupViewModel.setGroupedMediaFromClassification(groupId) }
        }
    }

    val state = groupViewModel.groupedMediaUiState.value
    if (state.mediaItems.isNotEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBackNavigationClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            content = {
                ThumbnailGridViewer(state.mediaItems, onImageClickNavigation)
            }
        )
    }
}