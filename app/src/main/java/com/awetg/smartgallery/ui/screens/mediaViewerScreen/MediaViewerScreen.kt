package com.awetg.smartgallery.ui.screens.mediaViewerScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MediaViewerScreen(
    onBackNavigationClick: () -> Unit,
    viewerArgument: ViewerArgument,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val showBars = rememberSaveable { (mutableStateOf(true)) }
    val pagerState = rememberPagerState(viewerArgument.mediaIndex)
    val imageCount = viewModel.getMediaCount(viewerArgument.groupIndex, viewerArgument.groupType)

    Scaffold(
        backgroundColor = Color.Black,
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            if (showBars.value) ViewerTopAppBar(onBackNavigationClick)
            else TopAppBar(backgroundColor = Color.Transparent, elevation = 0.dp) {}
        },
        bottomBar = { if (showBars.value) ViewerBottomBar() },
        content = {
            HorizontalPager(count = imageCount, state = pagerState) { page ->
                val mediaItem = viewModel.getNextMediaItemUri(page, viewerArgument.groupIndex, viewerArgument. groupType)
                val state = rememberZoomableState(
                    minScale = 1f,
                    maxScale = 8f
                )
                mediaItem?.let {
                    Zoomable(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onTap = { showBars.value = !showBars.value },
                    ) {
                        ImageBox(it)
                    }
                }
            }
        }
    )
}

@Composable
fun ImageBox(mediaItem:  MediaItem) {
    val imagePainterState = rememberAsyncImagePainter(mediaItem.uri)
    Image(
        painter = imagePainterState,
        contentScale = ContentScale.FillWidth,
        contentDescription = "",
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
    )
}