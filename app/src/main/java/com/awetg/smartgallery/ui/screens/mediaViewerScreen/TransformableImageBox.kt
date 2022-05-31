package com.awetg.smartgallery.ui.screens.mediaViewerScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

//@SuppressLint("UnrememberedAnimatable")
//@Composable
//fun TransformableImageBox(imageBitmap: ImageBitmap) {
//    val scope = rememberCoroutineScope()
//    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//    var scale by remember { mutableStateOf(1f) }
//    val minScale = 0.8f
//    val maxScale = 8f
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    val transformableState = rememberTransformableState { zoomChange, _, _ ->
//        scale = scale.times(zoomChange).coerceIn(minScale, maxScale)
////        scale *= zoomChange
////        offset += offsetChange
//    }
//
//        val velocityTracker = VelocityTracker()
//        val translateX = Animatable(0f)
//        val translateY = Animatable(0f)
//
//    val onDrag = { pointerInputChange: PointerInputChange, dragChange: Offset -> Unit
//        if (scale > 1f) {
//        scope.launch {
//            pointerInputChange.consumePositionChange()
//            translateX.snapTo(translateX.value + dragChange.x)
//            translateY.snapTo(translateY.value + dragChange.y)
//            velocityTracker.addPosition(pointerInputChange.uptimeMillis, pointerInputChange.position)
//            Log.d("smartImagesWorker","OnDrag-translateX: ${translateX.value}, OnDrag-translateY: ${translateY.value}")
////            offset += dragChange
//        }
//        }
//    }
//
//
//
//    var childWidth by remember { mutableStateOf(0) }
//    var childHeight by remember { mutableStateOf(0) }
//    LaunchedEffect(
//        childHeight,
//        childWidth,
//        scale,
//    ) {
//        val maxX = (childWidth * scale - constraints.maxWidth)
//            .coerceAtLeast(0F) / 2F
//        val maxY = (childHeight * scale - constraints.maxHeight)
//            .coerceAtLeast(0F) / 2F
//        translateX.updateBounds(-maxX, maxX)
//        translateY.updateBounds(-maxY, maxY)
//    }
//
//    Box(
//        Modifier
////            .graphicsLayer(
//////                scaleX = scale,
//////                scaleY = scale,
//////                translationX = translateX.value,
//////                translationY = translateY.value,
////            )
//            .pointerInput(Unit) {
//                forEachGesture {
//                    awaitPointerEventScope {
//                        val down = awaitFirstDown(requireUnconsumed = false)
//                        var drag: PointerInputChange?
//                        do {
//                            drag = awaitTouchSlopOrCancellation(down.id, onDrag)
//                        } while (drag != null && !drag.positionChangeConsumed())
//                        if (drag != null) {
//                            if (
//                                !drag(drag.id) { onDrag(it, it.positionChange()) }
//                            ) {
//                                if(scale > 1f) velocityTracker.resetTracking()
//                            } else {
//                                scope.launch {
//                                    val velocity = velocityTracker.calculateVelocity()
//                                    val tempOffset = Offset(velocity.x, velocity.y)
//                                    translateX.animateDecay(tempOffset.x / 2f, exponentialDecay())
//                                    translateY.animateDecay(tempOffset.y / 2f, exponentialDecay())
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            .transformable(state = transformableState)
//            .layout { measurable, constraints ->
//                val placeable =
//                    measurable.measure(constraints = constraints)
//                childHeight = placeable.height
//                childWidth = placeable.width
//                layout(
//                    width = constraints.maxWidth,
//                    height = constraints.maxHeight
//                ) {
//                    placeable.placeRelativeWithLayer(
//                        (constraints.maxWidth - placeable.width) / 2,
//                        (constraints.maxHeight - placeable.height) / 2
//                    ) {
//                        Log.d("smartImagesWorker","translateX: ${translateX.value}, translateY: ${translateY.value}")
//                        scaleX = scale
//                        scaleY = scale
//                        translationX = translateX.value
//                        translationY = translateY.value
//                    }
//                }
//            }
////            .pointerInput(Unit) {
////                detectTransformGestures(
////                    panZoomLock = false,
////                    onGesture = { centroid, pan, gestureZoom, _ ->
////                        scale = scale.times(gestureZoom).coerceIn(minScale, maxScale)
////                        if(scale > 1) {
////                            offset += (pan * scale)
////                        }else{
////                            offset = Offset.Zero
////                        }
////                    }
////                )
////            }
//            .background(Color.Transparent)
//    ) {
//
//        Image(
//            painter = BitmapPainter(imageBitmap),
//            contentScale = ContentScale.FillWidth,
//            contentDescription = "",
//            modifier = Modifier
////                .matchParentSize()
//                .background(color = Color.Transparent)
//        )
//    }
//}
//}
//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun ZoomableImage(
//    imageBitmap: ImageBitmap,
////    onLongPress: ((Offset) -> Unit)? = null,
////    onTap: ((Offset) -> Unit)? = null
//) {
//    val scope = rememberCoroutineScope()
//
//    var layout: LayoutCoordinates? = null
//
//    var scale by remember { mutableStateOf(1f) }
//    val minScale = 0.8f
//    val maxScale = 8f
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
////        Log.d("smartImagesWorker", "scale: $zoomChange")
//        scale = scale.times(zoomChange).coerceIn(minScale, maxScale)
//        if(scale > 1) {
//            offset  += panChange.times(scale)
//        }else{
//            offset = Offset.Zero
//        }
//    }
//
//    Box(
//        modifier = Modifier
////            .background(Color.Transparent)
//            .fillMaxSize()
//            .clipToBounds()
//            .transformable(state = transformableState)
//            .pointerInput(Unit) {
//                forEachGesture {
//                    awaitPointerEventScope {
//                        val down = awaitFirstDown(requireUnconsumed = false)
//                        drag(down.id) {
//                            if (layout == null) return@drag
//                            val maxX = layout!!.size.width * (scale - 1) / 2f
//                            val maxY = layout!!.size.height * (scale - 1) / 2f
//                            val targetTranslation = (it.positionChange() + offset)
//                            Log.d("smartImagesWorker", "Scale: $scale, maxX: $maxX, maxY: $maxY, targetTranslation: $targetTranslation")
//                            if (targetTranslation.x > -maxX && targetTranslation.x < maxX &&
//                                targetTranslation.y > -maxY && targetTranslation.y < maxY
//                            ) {
//                                offset = targetTranslation
//                                it.consumePositionChange()
//                            }
//                        }
//                    }
//                }
//            }
//    ) {
//        Image(
//            painter = BitmapPainter(imageBitmap),
//            contentDescription = "",
//            modifier = Modifier
////                .background(Color.Transparent)
//                .matchParentSize()
//                .onPlaced { layout = it }
//                .graphicsLayer(
//                    scaleX = scale,
//                    scaleY = scale,
//                    translationX = offset.x,
//                    translationY = offset.y
//                ),
//            contentScale = ContentScale.Fit
//        )
//
//        LaunchedEffect(transformableState.isTransformInProgress) {
//            if (!transformableState.isTransformInProgress) {
//                if (scale < 1f) {
//                    val originScale = scale
//                    val originTranslation = offset
//                    AnimationState(initialValue = 0f).animateTo(
//                        1f,
//                        SpringSpec(stiffness = Spring.StiffnessLow)
//                    ) {
//                        scale = originScale + (1 - originScale) * this.value
//                        offset = originTranslation * (1 - this.value)
//                    }
//                } else {
//                    if (layout == null) return@LaunchedEffect
//                    val maxX = layout!!.size.width * (scale - 1) / 2f
//                    val maxY = layout!!.size.height * (scale - 1) / 2f
//                    val target = Offset(
//                        offset.x.coerceIn(-maxX, maxX),
//                        offset.y.coerceIn(-maxY, maxY)
//                    )
//                    AnimationState(
//                        typeConverter = Offset.VectorConverter,
//                        initialValue = offset
//                    ).animateTo(target, SpringSpec(stiffness = Spring.StiffnessLow)) {
//                        offset = this.value
//                    }
//                }
//            }
//        }
//    }
//}