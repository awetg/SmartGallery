package com.awetg.smartgallery.services.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.common.util.FileUtil
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.services.mlmodels.ChineseWhispersClustering
import com.awetg.smartgallery.services.mlmodels.TensorFlowFaceNetModel
import com.awetg.smartgallery.services.mlmodels.MLKitFaceDetector
import com.google.mlkit.vision.common.InputImage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.NumberFormatException
import java.util.*
import kotlin.math.absoluteValue

class FaceClusterWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val galleryDatabase: GalleryDatabase
    ) : BaseWorker(context, parameters) {

    private val faceDetector = MLKitFaceDetector()
    private val tensorFlowFaceNetModel = TensorFlowFaceNetModel(applicationContext, FACE_NET_ASSET_NAME)

    private val encodingHashMap = HashMap<String, FloatArray>()
    private val faceAlignmentMap = HashMap<String, Float>()
    private var lastProcessedMediaItemId = -1L

    override suspend fun doWork(): Result {
        try {
            return withContext(Dispatchers.IO) {
                val jobType = inputData.getString(DATA_INPUT_KEY_JOB_TYPE)
                    ?: Result.failure()

                val progress = "Processing media..."
                setForeground(createForegroundInfo(progress, id))

                when (jobType) {
                    JOB_TYPE_ALL -> {
                        if (clusterFaceForAllMedia())
                            return@withContext Result.success()
                        else
                            return@withContext Result.failure()
                    }
                    JOB_TYPE_UPDATE -> {
                        val newItemsIds =
                            inputData.getLongArray(DATA_KEY_NEW_MEDIAS) ?: longArrayOf()
                        val deletedItemsIds =
                            inputData.getLongArray(DATA_KEY_DELETED_MEDIAS) ?: longArrayOf()
                        updateCluster(newItemsIds, deletedItemsIds)
                        return@withContext Result.success()
                    }
                    // TODO: implement partial face clustering when media count more than 400
                    JOB_TYPE_PARTIAL -> return@withContext Result.failure()
                    else -> return@withContext Result.failure()
                }

            }
        } catch (e: Exception) {
            if (encodingHashMap.isNotEmpty() && lastProcessedMediaItemId != -1L) {
                saveEncodingsToFile()
                saveFaceAlignmentsToFile()
            }
            return Result.failure()
        }
    }

    private suspend fun clusterFaceForAllMedia(): Boolean {
        // if external storage is non writeable (full or permission) return
        if (!FileUtil.isExternalStorageWritable()) return false

        FileUtil.createDirInExternalStorageCache(applicationContext, FACES_DIR)
        FileUtil.createDirInExternalStorage(applicationContext, CLUSTER_DIR)

        // using test dataset for demo
        val mediaItems = galleryDatabase.mediaItemDao.getMediaItemsByPath("face_dataset/").first()

        mediaItems.forEach { mediaItem ->
            try {
                val mediaItemBitmap = this.getMediaItemBitmap(mediaItem.uri)
                val inputImage = InputImage.fromFilePath(applicationContext, mediaItem.uri)
                val results = faceDetector.detectFaces(inputImage)
                results.await()
                if (results.isSuccessful) {
                    results.result.forEachIndexed { i, face ->
                        val faceBitmap = faceDetector.getCroppedFace(mediaItemBitmap, face)
                        if (faceBitmap != null) {
                            val mapKey = "${mediaItem.id}_$i"
                            encodingHashMap[mapKey] = tensorFlowFaceNetModel.getEmbedding(faceBitmap)
                            val alignmentValue = face.headEulerAngleY.absoluteValue + face.headEulerAngleX.absoluteValue + face.headEulerAngleZ.absoluteValue
                            faceAlignmentMap[mapKey] = alignmentValue
                            FileUtil.savePhotoToExternalStorageCache(applicationContext, faceBitmap, "$FACES_DIR/$mapKey.png")
                            lastProcessedMediaItemId = mediaItem.id!!
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "detect faces failed for ${mediaItem.name}")
                    results.exception?.printStackTrace()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to process media item ${mediaItem.name}")
                e.printStackTrace()
            }
        }
        if (encodingHashMap.isNotEmpty()) {
            val cw = ChineseWhispersClustering(encodingHashMap, 128)
            val clusterMap = cw.cluster()
            val mediaClassificationMap = HashMap<String, List<Long>>()
            clusterMap.forEach { (clusterId, files) ->
                val mostAlignedFace = faceAlignmentMap.asSequence().filter { files.contains(it.key) }.minByOrNull { it.value }
                val al = files.mapNotNull { faceAlignmentMap[it] }.minOrNull()
                if (mostAlignedFace != null) {
                    val srcFile = File(applicationContext.externalCacheDir, "$FACES_DIR/${mostAlignedFace.key}.png")
                    val destFile = File(applicationContext.getExternalFilesDir(null), "$CLUSTER_DIR/${clusterId}.png")
                    try {
                        srcFile.copyTo(destFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.d(LOG_TAG, "Unable to copy image to cluster folder!")
                    }
                    try {
                        val mediaIds = files.map { it.split("_")[0].toLong() }
                        mediaClassificationMap[clusterId.toString()] = mediaIds
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        Log.d(LOG_TAG, "error parsing medias store ids from string to long")
                    }
                }
            }
            if (mediaClassificationMap.isNotEmpty()) {
                val mediaClassifications = mediaClassificationMap.map { (className, ids) ->
                    MediaClassification(id = null, name = className, mediaItemIds = ids, type = MediaClassification.CLASSIFICATION_TYPE_CLUSTER)
                }
                galleryDatabase.mediaClassificationDao.insertAll(mediaClassifications = mediaClassifications)
            }
        }
        return true
    }

    private fun updateCluster(newItemsIds: LongArray, deletedItems: LongArray) {

    }

    private fun saveObjectToFile(obj: Any, fileName: String) {
        try {
            applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                val objectOutputStream = ObjectOutputStream(it)
                objectOutputStream.writeObject(obj)
                objectOutputStream.flush()
                objectOutputStream.close()
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(LOG_TAG, "error saving object to file")
        }
    }

    private fun saveEncodingsToFile() {
        saveObjectToFile(encodingHashMap, "encoding")
    }

    private fun readEncodingsFromFile(): HashMap<String, FloatArray>? {
        try {
            applicationContext.openFileInput("encoding").use {
                val objectInputStream = ObjectInputStream(it)
                val encodingHashMap: HashMap<String, FloatArray> = objectInputStream.readObject() as HashMap<String, FloatArray>
                objectInputStream.close()
                it.close()
                return encodingHashMap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(LOG_TAG, "error reading encoding hash map from file")
            return null
        }
    }

    private fun saveFaceAlignmentsToFile() {
        saveObjectToFile(faceAlignmentMap, "alignment")
    }

    private fun readFaceAlignmentFromFile(): HashMap<String, Float>? {
        try {
            applicationContext.openFileInput("alignment").use {
                val objectInputStream = ObjectInputStream(it)
                val faceAlignmentMap: HashMap<String, Float> = objectInputStream.readObject() as HashMap<String, Float>
                objectInputStream.close()
                it.close()
                return faceAlignmentMap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(LOG_TAG, "error reading encoding hash map from file")
            return null
        }
    }
}