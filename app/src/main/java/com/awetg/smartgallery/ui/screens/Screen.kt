package com.awetg.smartgallery.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

val tabIcons = Icons.Filled

sealed class Screen(val route: String, val title : String, val icon : ImageVector?) {
    object PhotosScreen : Screen(route = "photos_screen", title = "Photos", icon= tabIcons.Home)
    object SearchScreen : Screen( route = "search_screen", title = "Search", icon= tabIcons.Search)
    object LibraryScreen : Screen(route = "library_screen", title = "Library", icon= tabIcons.List)
    object MediaViewerScreen: Screen(route = "media_viewer_screen", title = "Viewer", null)
    object GroupedPhotosScreen: Screen(route = "grouped_photos_screen", title = "Photos", null)
    object SearchResultScreen: Screen(route = "string_search_screen", title = "String Search Screen", null)
//    object SearchResultScreen: Screen(route = "search_result_screen", title = "Search Result Screen", null)
}