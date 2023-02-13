package com.awetg.smartgallery.services.workers

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.work.*
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.data.entities.MediaItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MediaScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val galleryDatabase: GalleryDatabase
    ) : BaseWorker(context, parameters) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val type = inputData.getString(DATA_INPUT_KEY_JOB_TYPE)
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
                    val mediaUpdate = mediaStoreUpdate(lastCount)
                     val data = Data.Builder()
                            .putLongArray(DATA_KEY_NEW_MEDIAS, mediaUpdate.newItems)
                            .putLongArray(DATA_KEY_DELETED_MEDIAS, mediaUpdate.deletedItems)
                            .putInt(DATA_OUTPUT_KEY_MEDIA_COUNT, mediaUpdate.count)
                            .build()
                    return@withContext Result.success(data)

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
                Log.e(LOG_TAG, "Cursor is null!")
            }
            if (reSync) galleryDatabase.mediaItemDao.deleteAll()

            galleryDatabase.mediaItemDao.insertAll(mediaItems.toList())

            return mediaItems.count()

        } catch (e: Exception) {
            Log.e(LOG_TAG, e.toString())
            return 0
        }
    }

    private suspend fun mediaStoreUpdate(lastCount: Int): MediaUpdate {
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
                Log.e(LOG_TAG, "Cursor is null!")
            }

            if (mediaItems.count() != lastCount) {
                val storedItems = galleryDatabase.mediaItemDao.getMediaItemsByModifiedAt().first().toHashSet()
                val currentItems = mediaItems.toHashSet()

                val deletedItems = storedItems.minus(currentItems)
                var deletedItemsIds = longArrayOf()
                if (deletedItems.isNotEmpty()) {
                    galleryDatabase.mediaItemDao.deleteMediaItems(deletedItems.toList())
                    deletedItemsIds = deletedItems.map { it.mediaStoreId }.toLongArray()
                }

                val newItems = currentItems.minus(storedItems)
                var newItemsIds = longArrayOf()
                if (newItems.isNotEmpty()) {
                    galleryDatabase.mediaItemDao.insertAll(newItems.toList())
                    newItemsIds = newItems.map { it.mediaStoreId }.toLongArray()
                }

                val newCount =  lastCount - deletedItems.count() + newItems.count()
                return MediaUpdate(newCount, newItemsIds, deletedItemsIds)
            }

            return MediaUpdate(lastCount, longArrayOf(), longArrayOf())

        } catch (e: Exception) {
            Log.e(LOG_TAG, e.toString())
            return MediaUpdate(lastCount, longArrayOf(), longArrayOf())
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
                    width = width
                )
                mediaItems.add(mediaItem)
            }
        }
        return mediaItems.toList()
    }

    private inner class MediaUpdate(val count: Int, val newItems: LongArray, val deletedItems: LongArray)
}
