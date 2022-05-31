package com.awetg.smartgallery.services

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
    ): ListenableWorker = MediaScanWorker(appContext, workerParameters, galleryDatabase)
}