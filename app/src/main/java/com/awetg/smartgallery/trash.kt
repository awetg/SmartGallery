package com.awetg.smartgallery

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import com.awetg.smartgallery.data.entities.MediaItem

//fun getImageBitmap(mediaItem: MediaItem): ImageBitmap? {
//    return try {
//        val thumbnail: Bitmap = if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
//            val w = when{
//                mediaItem.width < 400 -> mediaItem.width
//                mediaItem.width < 800 -> mediaItem.width / 2
//                else -> mediaItem.width / 3
//            }
//            val h = when {
//                mediaItem.height < 400 -> mediaItem.height
//                mediaItem.height < 800 -> mediaItem.height / 2
//                else -> mediaItem.height / 3
//            }
//            resolver.loadThumbnail(mediaItem.uri, Size(w, h), null)
//        } else {
//            MediaStore.Images.Thumbnails.getThumbnail(resolver, mediaItem.mediaStoreId, MediaStore.Images.Thumbnails.MINI_KIND, null)
//        }
//        thumbnail.asImageBitmap()
//    } catch (e: IOException) {
//        Log.e(LOG_TAG, "getImageBitmap exception: ${e.message}")
//        null
//    }
//}


fun getBitmapV2(mediaItem: MediaItem?, resolver: ContentResolver): Bitmap? {
    return mediaItem?.let {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(resolver, it.uri)

        } else {
            val source = ImageDecoder.createSource(resolver, it.uri)
            ImageDecoder.decodeBitmap(source)
        }
    }
}
fun getBitmap(mediaItem: MediaItem?, resolver: ContentResolver): Bitmap? {
    return mediaItem?.let { it ->
        resolver.openFileDescriptor(it.uri, "r")?.let { BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }
    }
}