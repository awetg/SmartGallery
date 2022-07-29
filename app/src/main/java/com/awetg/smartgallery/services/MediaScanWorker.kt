package com.awetg.smartgallery.services

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.*
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.data.entities.MediaItem
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MediaScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val galleryDatabase: GalleryDatabase
    ) : BaseWorker(context, parameters) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val type = inputData.getString(DATA_INPUT_KEY_WORK_TYPE)
                ?: Result.failure()
            val lastCount = inputData.getInt(DATA_INPUT_KEY_MEDIA_COUNT, 0)

            val progress = "Scanning media..."
            setForeground(createForegroundInfo(progress, id))

            when(type){
                MEDIA_SCAN_TYPE_SYNC -> {
                    val newCount = fullMediaSync(false)
                    return@withContext Result.success(
                        Data.Builder().putInt(DATA_OUTPUT_KEY_MEDIA_COUNT, newCount).build()
                    )
                }

                MEDIA_SCAN_TYPE_RE_SYNC -> {
                    val newCount = fullMediaSync(true)
                    return@withContext Result.success(
                        Data.Builder().putInt(DATA_OUTPUT_KEY_MEDIA_COUNT, newCount).build()
                    )

                }
                MEDIA_SCAN_TYPE_UPDATE -> {
                    val newCount = mediaStoreUpdate(lastCount)
                    return@withContext Result.success(
                        Data.Builder().putInt(DATA_OUTPUT_KEY_MEDIA_COUNT, newCount).build()
                    )

                }
                else -> return@withContext Result.failure()
            }
        }

    }

    private fun fullMediaSync(reSync: Boolean): Int {
        try {
            var mediaItems = listOf<MediaItem>()

            val cursor = applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                defaultImageProjection,
                null,
                null,
                defaultMediaSortOrder
            )

            cursor?.use {
                if (it.count > 0) mediaItems =  getMediaItemsFromCursor(it)
            } ?: kotlin.run {
                Log.e("smartImagesWorker", "Cursor is null!")
            }
            if (reSync) galleryDatabase.mediaItemDao.deleteAll()

            galleryDatabase.mediaItemDao.insertAll(mediaItems.toList())

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
            val detector = FaceDetection.getClient(options)
            var detectedFaces = 0

            val contentResolver = applicationContext.contentResolver
            val getBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                {uri: Uri -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(applicationContext.contentResolver, uri))}
            } else {
                {uri: Uri -> MediaStore.Images.Media.getBitmap(contentResolver, uri) }
            }

            mediaItems.forEach {
                val bitmap: Bitmap
                try {
                    bitmap = getBitmap(it.uri)
                    val image = InputImage.fromFilePath(applicationContext, it.uri)
                    val result = detector.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                detectedFaces += faces.size
                                val face = faces.first()
                                val rect = face.boundingBox
//                                val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
//                                val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degree
                                var width = rect.width()
                                var height = rect.height()
                                val left = if (rect.left < 0) 0 else rect.left
                                val top = if (rect.top < 0) 0 else rect.top
                                if ( (left + width) > bitmap.width ){
                                    width = bitmap.width - left
                                }
                                if ( (top + height ) > bitmap.height ){
                                    height = bitmap.height - top
                                }
                                if (left < 0 || top < 0) {
                                    Log.e("smartImagesWorker", "Negative rect")
                                } else {
                                val croppedBitmap = Bitmap.createBitmap( bitmap , left , top , width , height )
                                if (savePhoto(it.mediaStoreId.toString(), croppedBitmap))
                                    Log.d("smartImagesWorker", "Saved photo")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("smartImagesWorker", "detector.process image failed")
                            e.printStackTrace()
                        }
                } catch (e: Exception){
                    Log.e("smartImagesWorker", "Failed to read bitmap image")
                    e.printStackTrace()
                }
            }
            Log.d("smartImagesWorker", "Faces detected: $detectedFaces")

            return mediaItems.count()

        } catch (e: Exception) {
            Log.e("smartImagesWorker", e.toString())
            return 0
        }
    }

    private suspend fun mediaStoreUpdate(lastCount: Int): Int {
        try {
            var mediaItems = listOf<MediaItem>()
//            arrayOf(lastSync.toString())

            val cursor = applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                defaultImageProjection,
                null,
                null,
                defaultMediaSortOrder
            )

            cursor?.use {
                if (it.count > 0) mediaItems =  getMediaItemsFromCursor(it)
            } ?: kotlin.run {
                Log.e("smartImagesWorker", "Cursor is null!")
            }

            if (mediaItems.count() != lastCount) {
                val storedItems = galleryDatabase.mediaItemDao.getMediaItems().first().toHashSet()
                val currentItems = mediaItems.toHashSet()

                val deletedItems = storedItems.minus(currentItems)
                galleryDatabase.mediaItemDao.deleteMediaItems(deletedItems.toList())

                val newItems = currentItems.minus(storedItems)
                galleryDatabase.mediaItemDao.insertAll(newItems.toList())

                return lastCount - deletedItems.count() + newItems.count()
            }

            return lastCount

        } catch (e: Exception) {
            Log.e("smartImagesWorker", e.toString())
            return lastCount
        }
    }

    private fun getMediaItemsFromCursor(cursor: Cursor): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        cursor.let {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val relativePathColumn =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            val dateModifiedColumn =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val dateCreatedColumn =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val heightColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val widthColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val size = it.getLong(sizeColumn)
                val dateCreated = it.getLong(dateCreatedColumn)
                val dateModified = it.getLong(dateModifiedColumn)
                val relativePath = it.getString(relativePathColumn)
                val fullPath = relativePath + name
                val height = it.getInt(heightColumn)
                val width = it.getInt(widthColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val mediaItem = MediaItem(
                    id = null,
                    name = name,
                    parentPath = relativePath,
                    fullPath = fullPath,
                    modifiedAt = dateModified,
                    createdAt = dateCreated,
                    deletedAt = -1,
                    size = size,
                    mediaStoreId = id,
                    type = MediaItem.MEDIA_TYPE_IMAGE,
                    uri = contentUri,
                    height = height,
                    width = width,
                )
                mediaItems.add(mediaItem)
            }
        }
        return mediaItems.toList()
    }

    private fun savePhoto(displayName: String, bitmap: Bitmap): Boolean {
        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        return try {
            applicationContext.contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                applicationContext.contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)) {
                        throw IOException("can't save bitmap")
                    }
                }
            } ?: throw IOException("couldn't create MediaStore entry")
            true
        } catch (e: IOException) {
            Log.e("smartImagesWorker", "Error saving photo")
            e.printStackTrace()
            false
        }
    }
}


inline fun <T> sdk29AndUp(onSdk29: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onSdk29()
    } else null
}