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
import com.awetg.smartgallery.common.ALBUM_GROUP
import com.awetg.smartgallery.ui.screens.searchScreen.SearchScreen
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.screens.groupedPhotosScreen.GroupedPhotosScreen
import com.awetg.smartgallery.ui.screens.libraryScreen.LibraryScreen
import com.awetg.smartgallery.ui.screens.mediaViewerScreen.MediaViewerScreen
import com.awetg.smartgallery.ui.screens.mediaViewerScreen.ViewerArgument
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosScreen
import com.awetg.smartgallery.ui.screens.stringSearchScreen.StringSearchScreen
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
            if (destination.route?.contains(Screen.MediaViewerScreen.route) == true) {
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
            Screen.GroupedPhotosScreen.route + "?groupIndex={groupIndex}&groupType={groupType}",
            listOf(
                navArgument(
                    name = "groupIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "groupType"
                ) {
                    type = NavType.StringType
                    defaultValue = ALBUM_GROUP
                },
            )
        ) {
            val groupIndex = it.arguments?.getInt("groupIndex") ?: -1
            val groupType = it.arguments?.getString("groupType") ?: ALBUM_GROUP
            GroupedPhotosScreen(groupIndex, groupType, onBackNavigationClick, onItemClickNavigation)
        }

        composable(
            Screen.MediaViewerScreen.route + "?mediaIndex={mediaIndex}&groupIndex={groupIndex}&groupType={groupType}",
            listOf(
                navArgument(
                    name = "mediaIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "groupIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "groupType"
                ) {
                    type = NavType.StringType
                    defaultValue = ALBUM_GROUP
                },
            )
        ) {
            val mediaIndex = it.arguments?.getInt("mediaIndex") ?: -1
            val groupIndex = it.arguments?.getInt("groupIndex") ?: -1
            val groupType = it.arguments?.getString("groupType") ?: ALBUM_GROUP
            val viewerArgument = ViewerArgument(mediaIndex, groupIndex, groupType)
            MediaViewerScreen(onBackNavigationClick, viewerArgument)
        }

        composable(Screen.SearchResultScreen.route) {
            StringSearchScreen(onBackNavigationClick)
        }
    }
}

