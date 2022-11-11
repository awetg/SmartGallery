package com.awetg.smartgallery.services.workers

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.awetg.smartgallery.R
import com.awetg.smartgallery.common.FOREGROUND_WORK_NOTIFICATION_CHANNEL_ID
import com.awetg.smartgallery.common.FOREGROUND_WORK_NOTIFICATION_ID
import com.awetg.smartgallery.common.LOG_TAG
import com.awetg.smartgallery.data.entities.MediaItem
import java.util.*

abstract class BaseWorker(private val context: Context, parameters: WorkerParameters): CoroutineWorker(context, parameters) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
            NotificationManager

    companion object {
        val defaultImageProjection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.ORIENTATION,
        )
        const val defaultMediaSortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        const val defaultSelection = MediaStore.Images.Media.DATE_MODIFIED + " => ?"
    }

    fun createForegroundInfo(progress: String, workerId: UUID): ForegroundInfo {
        val id = FOREGROUND_WORK_NOTIFICATION_CHANNEL_ID
        val title = "Processing media files."
        val cancel = "Cancel scan"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workerId)


        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_baseline_sync_24)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(FOREGROUND_WORK_NOTIFICATION_ID, notification)
    }

    val getMediaItemThumbnail = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        {mediaItem: MediaItem, size: android.util.Size ->
            applicationContext.contentResolver.loadThumbnail(mediaItem.uri, size, null)
        }
    } else {
        { mediaItem: MediaItem, _: android.util.Size ->
            val opts = BitmapFactory.Options()
            opts.inMutable = true
            MediaStore.Images.Thumbnails.getThumbnail(
                applicationContext.contentResolver,
                mediaItem.mediaStoreId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                opts
            )
        }
    }

    val getMediaItemBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        {uri: Uri -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(applicationContext.contentResolver, uri)
        ) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
        }
    } else {
        {uri: Uri -> MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, uri) }
    }
}