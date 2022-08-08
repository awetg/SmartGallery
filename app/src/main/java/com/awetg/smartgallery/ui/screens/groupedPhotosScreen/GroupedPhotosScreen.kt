package com.awetg.smartgallery.ui.screens.groupedPhotosScreen

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.awetg.smartgallery.common.ALBUM_GROUP
import com.awetg.smartgallery.common.CLUSTER_DIR
import com.awetg.smartgallery.common.CLUSTER_GROUP
import com.awetg.smartgallery.ui.components.ThumbnailGridViewer
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel

@Composable
fun GroupedPhotosScreen(
    groupIndex: Int,
    groupType: String,
    onBackNavigationClick: () -> Unit,
    onImageClickNavigation: (String) -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val state = when(groupType) {
        ALBUM_GROUP -> viewModel.albumUiState.value.albums
        CLUSTER_GROUP -> viewModel.clusterUiState.value.clusters
        else -> emptyList()
    }
    state.elementAt(groupIndex).let { mediaItems ->
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
                ThumbnailGridViewer(mediaItems, onImageClickNavigation, groupIndex, groupType)
            }
        )
    }
}