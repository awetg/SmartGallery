package com.awetg.smartgallery.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.theme.AppBarColor
import com.awetg.smartgallery.ui.theme.AppBarTextColor

@Composable
fun BottomBar(navController: NavController){

    val tabs = listOf(
        Screen.PhotosScreen,
        Screen.SearchScreen,
        Screen.LibraryScreen,
    )

    BottomNavigation(
        backgroundColor = AppBarColor,
        elevation = 0.dp
    ){
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        tabs.map {
            BottomNavigationItem(
                icon= {
                    it.icon?.let { icon ->
                        Icon(
                            modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 6.dp),
                            imageVector = icon,
                            contentDescription = it.title,
                            tint = if (currentDestination?.hierarchy?.any { h -> h.route == it.route } == true) MaterialTheme.colors.secondary
                            else AppBarTextColor
                        )
                    }
                },
                label= {
                    Text(
                        text = it.title,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                },
                selected = currentDestination?.hierarchy?.any { h -> h.route == it.route } == true,
                selectedContentColor= MaterialTheme.colors.secondary,
                unselectedContentColor= AppBarTextColor,
                onClick = {
                    navController.navigate(it.route) {
                        // https://developer.android.com/jetpack/compose/navigation#bottom-nav
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // re selecting the same item
                        launchSingleTop = true
                        // Restore state when re selecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }

    }
}