package com.awetg.smartgallery.ui.screens.searchScreen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.awetg.smartgallery.R
import com.awetg.smartgallery.common.CLUSTER_GROUP
import com.awetg.smartgallery.common.util.FileUtil
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import com.awetg.smartgallery.ui.theme.SearchBarColor
import com.awetg.smartgallery.ui.theme.AppBarTextColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    onItemClickNavigation: (String) -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val mlJobState = viewModel.mlJobState.value
    val clusterUiState = viewModel.clusterUiState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
    ) {
        LazyColumn {
            stickyHeader {  SearchBar{onItemClickNavigation(Screen.SearchResultScreen.route)} }

            if (mlJobState.clusterJobComplete && clusterUiState.clusters.isNotEmpty()) {
                item {
                    Text(
                        text = "People",
                    )
                }
                //Horizontal Scroll view
                item {
                    LazyRow {
                        items(clusterUiState.clusters.count()) { i ->
                            val file = FileUtil.getClusterPhoto(LocalContext.current, clusterUiState.clusters.elementAt(i).first().clusterId)
                            if (file != null) {
                                Card(
                                    modifier = Modifier
                                        .width(88.dp)
                                        .height(88.dp)
                                        .padding(5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            onItemClickNavigation(
                                                Screen.GroupedPhotosScreen.route + "?groupIndex=${i}&groupType=${CLUSTER_GROUP}"
                                            )
                                        },
                                    elevation = 5.dp
                                ) {
                                    val imagePainterState = rememberAsyncImagePainter(file)
                                    Image(
                                        painter = imagePainterState,
                                        contentDescription = "profile Image",
                                        contentScale = ContentScale.FillWidth,
                                    )

                                    Spacer(modifier = Modifier.padding(5.dp))
                                }
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
            .padding(0.dp, 8.dp, 0.dp, 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(40))
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