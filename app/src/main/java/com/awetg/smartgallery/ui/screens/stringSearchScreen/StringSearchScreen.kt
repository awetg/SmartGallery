package com.awetg.smartgallery.ui.screens.stringSearchScreen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.ui.theme.AppBarColor
import com.awetg.smartgallery.ui.theme.AppBarTextColor

@Composable
fun StringSearchScreen(
    onBackNavigationClick: () -> Unit,
    viewModel: SearchTextViewModel = hiltViewModel()
) {
    Log.d(LOG_TAG, "SearchResultScreen draw")

    val searchTextState by viewModel.searchTextState

    val onSearchClick = { value:String ->
        Log.d(LOG_TAG, "Search: $value")
        Unit
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            SearchInputBar(
                value = searchTextState,
                onValueChanged = {viewModel.updateSearchTextState(it)},
                onSearchClicked = onSearchClick,
                onBackButtonClicked = onBackNavigationClick
            )
        }
    ) {

    }

}

@Composable
fun SearchInputBar(
    value: String,
    onValueChanged: (String) -> Unit,
    onSearchClicked: (String) -> Unit,
    onBackButtonClicked: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 30 * density
                val y = size.height - strokeWidth / 2

                drawLine(
                    Color.Red,
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )
            },
        color = AppBarColor
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = { onValueChanged(it) },
            placeholder = {
                Text(
                    text = "Search here ...",
                    fontSize = 18.sp,
                    color = AppBarTextColor
                )
            },
            textStyle = TextStyle(color = AppBarTextColor, fontSize = 18.sp, fontWeight = FontWeight.Normal),
            leadingIcon = {
                IconButton(onClick = onBackButtonClicked) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (value.isNotEmpty()) onValueChanged("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50), // The TextFiled has rounded corners top left and right by default
            colors = TextFieldDefaults.textFieldColors(
                textColor = AppBarTextColor,
                cursorColor = MaterialTheme.colors.secondary,
                leadingIconColor = AppBarTextColor,
                trailingIconColor = AppBarTextColor,
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearchClicked(value) }
            )
        )
    }

}