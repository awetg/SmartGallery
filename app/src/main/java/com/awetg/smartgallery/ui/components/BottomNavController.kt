package com.awetg.smartgallery.ui.components

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.awetg.smartgallery.ui.screens.searchScreen.SearchScreen
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.screens.albumPhotosScreen.AlbumPhotosScreen
import com.awetg.smartgallery.ui.screens.libraryScreen.LibraryScreen
import com.awetg.smartgallery.ui.screens.mediaViewerScreen.MediaViewerScreen
import com.awetg.smartgallery.ui.screens.mediaViewerScreen.ViewerArgument
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosScreen
import com.awetg.smartgallery.ui.screens.searchResultScreen.SearchResultScreen
import com.awetg.smartgallery.ui.theme.graySurface
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun BottomNavController() {
    val navController = rememberNavController()

    val onItemClickNavigation = { route: String -> navController.navigate(route)}
    val onBackNavigationClick = { navController.navigateUp(); Unit }

    val systemUiController = rememberSystemUiController()
    val isDarkMode = isSystemInDarkTheme()
    val onDestinationChane = { _: NavController, destination: NavDestination, _: Bundle? ->
            if (destination?.route?.contains(Screen.MediaViewerScreen.route) == true) {
                systemUiController.setSystemBarsColor(
                    color = Color.Black
                )
            } else {
                if(isDarkMode){
                    systemUiController.setSystemBarsColor(
                        color = graySurface
                    )
                }else{
                    systemUiController.setSystemBarsColor(
                        color = Color.White
                    )
                }
            }
    }
    navController.addOnDestinationChangedListener(onDestinationChane)

    NavHost(navController, startDestination = Screen.PhotosScreen.route) {
        composable(Screen.PhotosScreen.route) {
            ScaffoldWithBars(navController) { PhotosScreen(onItemClickNavigation) }
        }
        composable(Screen.SearchScreen.route) {
            ScaffoldWithBars(navController) { SearchScreen(onItemClickNavigation) }
        }

        composable(Screen.LibraryScreen.route) {
            ScaffoldWithBars(navController) { LibraryScreen(onItemClickNavigation) }
        }

        composable(
            Screen.AlbumPhotosScreen.route + "?albumIndex={albumIndex}",
            listOf(
                navArgument(
                    name = "albumIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
            )
        ) {
            val albumIndex = it.arguments?.getInt("albumIndex") ?: -1
            AlbumPhotosScreen(albumIndex, onBackNavigationClick, onItemClickNavigation)
        }

        composable(
            Screen.MediaViewerScreen.route + "?mediaIndex={mediaIndex}&albumIndex={albumIndex}",
            listOf(
                navArgument(
                    name = "mediaIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "albumIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
            )
        ) {
            val mediaIndex = it.arguments?.getInt("mediaIndex") ?: -1
            val albumIndex = it.arguments?.getInt("albumIndex") ?: -1
            val viewerArgument = ViewerArgument(mediaIndex, albumIndex)
            MediaViewerScreen(onBackNavigationClick, viewerArgument)
        }

        composable(Screen.SearchResultScreen.route) {
            SearchResultScreen(onBackNavigationClick)
        }
    }
}

