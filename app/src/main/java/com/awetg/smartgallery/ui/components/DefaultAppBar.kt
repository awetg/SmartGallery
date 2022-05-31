package com.awetg.smartgallery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awetg.smartgallery.R
import com.awetg.smartgallery.ui.theme.AppBarColor
import com.awetg.smartgallery.ui.theme.AppNameColor
import com.awetg.smartgallery.ui.theme.graySurface

@Composable
fun DefaultAppBar() {
    TopAppBar(
        backgroundColor = AppBarColor,
        elevation = 0.dp,
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                color = AppNameColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }
    )
}