package com.awetg.smartgallery.services.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.awetg.smartgallery.data.data.GalleryDatabase
import javax.inject.Inject

class MyWorkerFactory @Inject constructor(private val galleryDatabase: GalleryDatabase) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker ? {
        return when(workerClassName) {
            MediaScanWorker::class.java.name ->
                MediaScanWorker(appContext, workerParameters, galleryDatabase)
            FaceClusterWorker::class.java.name ->
                FaceClusterWorker(appContext, workerParameters, galleryDatabase)
            MobileNetClassificationWorker::class.java.name ->
                MobileNetClassificationWorker(appContext, workerParameters, galleryDatabase)
            YoloObjectDetectionWorker::class.java.name ->
                YoloObjectDetectionWorker(appContext, workerParameters, galleryDatabase)
            else ->
                null
        }
    }
}