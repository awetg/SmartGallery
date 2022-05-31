package com.awetg.smartgallery.ui.screens.albumPhotosScreen

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.awetg.smartgallery.ui.components.ThumbnailGridViewer
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel

@Composable
fun AlbumPhotosScreen(
    albumIndex: Int,
    onBackNavigationClick: () -> Unit,
    onImageClickNavigation: (String) -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val state = viewModel.albumUiState.value
    state.albums.elementAt(albumIndex).let { mediaItems ->
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
                ThumbnailGridViewer(mediaItems, onImageClickNavigation, albumIndex)
            }
        )
    }
}