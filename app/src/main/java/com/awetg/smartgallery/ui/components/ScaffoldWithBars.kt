package com.awetg.smartgallery.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun ScaffoldWithBars(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
){
    Scaffold(topBar = { DefaultAppBar()}, bottomBar = { BottomBar(navController)}) {
        Surface(Modifier.padding(it)){content(it)}
    }
}