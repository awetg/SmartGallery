package com.awetg.smartgallery.ui.screens.mediaViewerScreen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class ViewerActions(val title: String, val icon: ImageVector?) {
    object Share: ViewerActions("Share", Icons.Outlined.Share)
    object Edit: ViewerActions("Edit", Icons.Outlined.Edit)
    object Delete: ViewerActions("Delete", Icons.Filled.DeleteOutline)
}

@Composable
fun ViewerBottomBar() {
    val actions = listOf(
        ViewerActions.Share,
        ViewerActions.Edit,
        ViewerActions.Delete
    )
    BottomNavigation(
        elevation = 0.dp,
        backgroundColor = Color.Black.copy(alpha = 0.2f)
    ) {

        actions.map {
            BottomNavigationItem(
                icon = {
                    it.icon?.let { icon ->
                        Icon(
                            modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 6.dp),
                            imageVector = icon,
                            contentDescription = it.title,
                        )
                    }
                },
                label = {
                    Text(
                        text = it.title,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                },
                selected = false,
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White,
                onClick = {
                    Log.d("smartImagesWorker", "Clicked Action: ${it.title}")
                }
            )
        }
    }
}