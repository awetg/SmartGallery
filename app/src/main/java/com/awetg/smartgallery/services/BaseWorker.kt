package com.awetg.smartgallery.services

import android.app.NotificationManager
import android.content.Context
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.awetg.smartgallery.R
import com.awetg.smartgallery.common.FOREGROUND_WORK_NOTIFICATION_CHANNEL_ID
import com.awetg.smartgallery.common.FOREGROUND_WORK_NOTIFICATION_ID
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
        val title = "Scanning your media files."
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
}