package com.awetg.smartgallery.ui.screens.libraryScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.awetg.smartgallery.common.ALBUM_GROUP
import com.awetg.smartgallery.common.CLUSTER_GROUP
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onImageClickNavigation: (String) -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val state = viewModel.albumUiState.value

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 0.dp)
    ) {
        LazyVerticalGrid(
            modifier = Modifier.wrapContentHeight(),
            cells = GridCells.Fixed(2)
        ) {
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Albums",
                        fontSize = 32.sp
                    )
                }
            }
            items(state.albums.count()) { i ->
                AlbumThumbnailBox(state.albums.elementAt(i).first()) {
                    onImageClickNavigation(
                        Screen.GroupedPhotosScreen.route + "?groupType=${ALBUM_GROUP}&groupId=${i}"
                    )
                }
            }
            item {
                Row(Modifier.height(100.dp)) {

                }
            }
        }
    }
}


@Composable
fun AlbumThumbnailBox(mediaItem: MediaItem, onClick: () -> Unit) {
    val imagePainterState = rememberAsyncImagePainter(mediaItem.uri)
    val albumName = mediaItem.parentPath.split("/").last { it.isNotEmpty() }
    Column(Modifier.padding(12.dp)) {

        Surface(
            modifier = Modifier
                .aspectRatio(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = imagePainterState,
                contentDescription = "",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.LightGray, BlendMode.Darken)
            )
        }
        Text(
            modifier = Modifier.padding(0.dp, 4.dp),
            text = albumName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}