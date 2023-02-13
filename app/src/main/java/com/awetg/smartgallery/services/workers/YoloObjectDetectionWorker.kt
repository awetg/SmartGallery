package com.awetg.smartgallery.services.workers

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.work.WorkerParameters
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.services.mlmodels.PytorchYolo5Model
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class YoloObjectDetectionWorker@AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val galleryDatabase: GalleryDatabase
) : BaseWorker(context, parameters) {

    private val pytorchYolo5Model = PytorchYolo5Model(applicationContext, PYTORCH_YOLO5_MODEL)
    override suspend fun doWork(): Result {
        try {
            return withContext(Dispatchers.IO) {
                val jobType = inputData.getString(DATA_INPUT_KEY_JOB_TYPE)
                    ?: Result.failure()

                val progress = "Detecting objects..."
                setForeground(createForegroundInfo(progress, id))

                when(jobType) {
                    JOB_TYPE_ALL -> {
                        if (detectObjectForAllMedias())
                            return@withContext Result.success()
                        else
                            return@withContext Result.failure()
                    }

                    else -> return@withContext Result.failure()
                }



            }
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private suspend fun detectObjectForAllMedias():Boolean {
        val mediaItems = galleryDatabase.mediaItemDao.getMediaItemsByModifiedAt().first()
        val mediaClassificationMap = HashMap<String, MutableList<Long>>()
        mediaItems.take(100).forEach{ mediaItem ->
            try {
                val mediaItemBitmap = this.getMediaItemThumbnail(mediaItem, Size(640, 640))
                val classNames = pytorchYolo5Model.detectObjects(mediaItemBitmap)
                if (classNames.isNotEmpty()) {
                    classNames.forEach {
                        if (mediaClassificationMap.containsKey(it)) {
                            mediaItem.id?.let { id -> mediaClassificationMap[it]!!.add(id) }
                        } else {
                            mediaItem.id?.let { id -> mediaClassificationMap[it] = mutableListOf(id) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to classify media item ${mediaItem.name}")
                e.printStackTrace()
            }
        }
        val mediaClassifications = mediaClassificationMap.map { (className, ids) ->
            MediaClassification(id = null, name = className, mediaItemIds = ids, type = MediaClassification.CLASSIFICATION_TYPE_OBJECT_DETECTION)
        }
        galleryDatabase.mediaClassificationDao.insertAll(mediaClassifications = mediaClassifications)
        return true
    }
}