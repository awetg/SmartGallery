package com.awetg.smartgallery.ui.screens.mediaViewerScreen

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ViewerTopAppBar(onBackNavigationClick: () -> Unit) {
    TopAppBar(
        title = {},
        elevation = 0.dp,
        backgroundColor = Color.Black.copy(alpha = 0.2f),
        contentColor = Color.White,
        navigationIcon = {
            IconButton(onClick = onBackNavigationClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

    )
}