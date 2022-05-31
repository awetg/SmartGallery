package com.awetg.smartgallery.ui.screens.searchScreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awetg.smartgallery.R
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.theme.SearchBarColor
import com.awetg.smartgallery.ui.theme.AppBarTextColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    onSearchBarClickNavigation: (String) -> Unit,
) {
    Log.d("smartImagesWorker", "SearchScreen draw")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
    ) {
        LazyColumn {
            stickyHeader {  SearchBar{onSearchBarClickNavigation(Screen.SearchResultScreen.route)} }
            item {
                Text(
                    text = "Recent List",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            //Horizontal Scroll view
            item {
                LazyRow {
                    items(count = 10) {
                        Card(
                            modifier = Modifier
                                .width(110.dp)
                                .height(120.dp)
                                .padding(0.dp, 5.dp, 5.dp, 0.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                            elevation = 5.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "profile Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.padding(5.dp))

                                Text(
                                    text = "Test",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Lists",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            items(count = 10) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(0.dp, 5.dp, 10.dp, 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    elevation = 5.dp
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.padding(5.dp))

                            Column {
                                Text(
                                    text = "Sample Test",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.padding(2.dp))

                                Text(
                                    text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(onClick: () -> Unit) {
    Box(
        Modifier
            .padding(0.dp, 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SearchBarColor)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart,
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
            ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier
                    .padding(12.dp)
//                    .alpha(ContentAlpha.medium)
                    .size(24.dp),
                tint = AppBarTextColor
            )
            Text(
                text = "Search here ...",
                color = AppBarTextColor,
                fontSize = 17.sp,
//                modifier = Modifier.alpha(ContentAlpha.medium)
            )
        }
    }
}