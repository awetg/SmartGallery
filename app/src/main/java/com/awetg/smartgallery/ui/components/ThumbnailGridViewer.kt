package com.awetg.smartgallery.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.awetg.smartgallery.common.ALBUM_GROUP
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.ui.screens.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThumbnailGridViewer(mediaItems:  List<MediaItem>, onImageClickNavigation: (String) -> Unit, groupIndex: Int, groupType: String = ALBUM_GROUP) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            cells = GridCells.Fixed(3)
        ) {
            items(mediaItems.count()) { i ->
                ThumbnailBox(mediaItems.elementAt(i)) {
                        onImageClickNavigation(
                            Screen.MediaViewerScreen.route + "?mediaIndex=${i}&groupIndex=${groupIndex}&groupType=${groupType}"
                        )
                }
            }
        }
    }
}

@Composable
fun ThumbnailBox(mediaItem: MediaItem, onClick: ()-> Unit) {
    val imagePainterState = rememberAsyncImagePainter(mediaItem.uri)
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = imagePainterState,
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}