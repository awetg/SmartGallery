package com.awetg.smartgallery.ui.screens.searchScreen

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.awetg.smartgallery.common.CLUSTER_GROUP
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.common.OBJECT_DETECTION_GROUP
import com.awetg.smartgallery.common.util.FileUtil
import com.awetg.smartgallery.ui.screens.Screen
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import com.awetg.smartgallery.ui.theme.SearchBarColor
import com.awetg.smartgallery.ui.theme.AppBarTextColor
import com.awetg.smartgallery.ui.theme.textButtonColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    onItemClickNavigation: (String) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val clusterUiState = searchViewModel.clusterUiState.value
    val objectDetectionUiState = searchViewModel.objectDetectionUiState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
    ) {
        LazyColumn {
            stickyHeader {  SearchBar{onItemClickNavigation(Screen.SearchResultScreen.route)} }

            item {
                SearchHeading(heading = "People")
            }
            if (!clusterUiState.isLoading && clusterUiState.classifications.isNotEmpty()) {
                //Horizontal Scroll view
                item {
                    LazyRow {
                        items(clusterUiState.classifications.count()) { i ->
                            val file = FileUtil.getClusterPhoto(LocalContext.current, clusterUiState.classifications.elementAt(i).name)
                            val groupId = clusterUiState.classifications.elementAt(i).id
                            if (file != null) {
                                Card(
                                    modifier = Modifier
                                        .width(88.dp)
                                        .height(88.dp)
                                        .padding(5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            onItemClickNavigation(
                                                Screen.GroupedPhotosScreen.route + "?groupType=${CLUSTER_GROUP}&groupId=${groupId}"
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
            else {
                item {
                    Text(
                        text = "Face index is not complete.",
                    )
                }
            }

            item { Spacer(modifier = Modifier.padding(16.dp)) }

            item {
                SearchHeading(heading = "Filters")
            }
            if (!objectDetectionUiState.isLoading && objectDetectionUiState.classifications.isNotEmpty()) {
                item {
                    LazyRow {
                        items(objectDetectionUiState.classifications.count()) { i ->
                            val groupId = objectDetectionUiState.classifications.elementAt(i).id
                            val name = objectDetectionUiState.classifications.elementAt(i).name
                            OutlinedButton(
                                modifier = Modifier
                                    .padding(4.dp, 8.dp),
                                shape = RoundedCornerShape(20),
                                border = BorderStroke(1.dp, Color.Gray),
                                onClick = {
                                    onItemClickNavigation(
                                        Screen.GroupedPhotosScreen.route + "?groupType=${OBJECT_DETECTION_GROUP}&groupId=${groupId}"
                                    )
                                }
                            ) {
                                Text(
                                    text = name,
                                    color = AppBarTextColor
                                )
                            }
                        }

                    }
                }
            } else {
                item {
                    Text(
                        text = "Image classification is not complete.",
                    )
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

@Composable
fun SearchHeading(heading: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = heading,
//            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(0.dp, 8.dp)
        )
        TextButton(onClick = { /*TODO*/ }) {
            Text(
                text = "View all",
                color = textButtonColor,
                fontSize = 14.sp,
            )
        }
    }
}



//items(classificationUiState.classifications.count()) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(100.dp)
//            .padding(0.dp, 5.dp, 10.dp, 5.dp)
//            .clip(RoundedCornerShape(10.dp))
//            .background(Color.White),
//        elevation = 5.dp
//    ) {
//        Column(
//            modifier = Modifier.padding(10.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                    contentDescription = "Profile Image",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clip(CircleShape)
//                )
//
//                Spacer(modifier = Modifier.padding(5.dp))
//
//                Column {
//                    Text(
//                        text = "Sample Test",
//                        color = Color.Black,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.padding(2.dp))
//
//                    Text(
//                        text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
//                        color = Color.Gray,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        }
//    }
//}