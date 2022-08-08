package com.awetg.smartgallery.services

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.WorkerParameters
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.common.util.FileUtil
import com.awetg.smartgallery.data.data.GalleryDatabase
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
import java.util.HashMap
import kotlin.math.absoluteValue

class FaceClusterWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val galleryDatabase: GalleryDatabase
    ) : BaseWorker(context, parameters) {

    private val faceDetector = MLKitFaceDetector()
    private val faceNetModel = FaceNetModel(applicationContext, FACE_NET_ASSET_NAME)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val jobType = inputData.getString(DATA_INPUT_KEY_FACE_CLUSTER_TYPE)
                ?: Result.failure()

            val progress = "Processing media..."
            setForeground(createForegroundInfo(progress, id))

            when(jobType) {
                FACE_CLUSTER_JOB_ALL -> {
                    if(clusterFaceForAllMedia())
                        return@withContext Result.success()
                    else
                        return@withContext Result.failure()
                }
                FACE_CLUSTER_JOB_UPDATE -> {
                    val newItemsIds = inputData.getLongArray(DATA_KEY_NEW_MEDIAS) ?: longArrayOf()
                    val deletedItemsIds = inputData.getLongArray(DATA_KEY_DELETED_MEDIAS) ?: longArrayOf()
                    updateCluster(newItemsIds, deletedItemsIds)
                    return@withContext Result.success()
                }
                // TODO: implement partial face clustering when media count more than 500
                FACE_CLUSTER_JOB_PARTIAL -> return@withContext Result.failure()
                else -> return@withContext Result.failure()
            }

        }
    }

    val getBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        {uri: Uri -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(applicationContext.contentResolver, uri))}
    } else {
        {uri: Uri -> MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, uri) }
    }

    private suspend fun clusterFaceForAllMedia(): Boolean {
        if (!FileUtil.isExternalStorageWritable()) return false

        FileUtil.createDirInExternalStorageCache(applicationContext, FACES_DIR)
        FileUtil.createDirInExternalStorage(applicationContext, CLUSTER_DIR)

        val mediaItems = galleryDatabase.mediaItemDao.getMediaItems().first()

        val encodingHashMap = HashMap<String, FloatArray>()
        val faceAlignmentMap = mutableMapOf<String, Float>()

        mediaItems.take(50).forEach { mediaItem ->
            try {
                val mediaItemBitmap = this.getBitmap(mediaItem.uri)
                val inputImage = InputImage.fromFilePath(applicationContext, mediaItem.uri)
                val results = faceDetector.detectFaces(inputImage)
                results.await()
                if (results.isSuccessful) {
                    results.result.forEachIndexed { i, face ->
                        val faceBitmap = faceDetector.getCroppedFace(mediaItemBitmap, face)
                        if (faceBitmap != null) {
                            val mapKey = "${mediaItem.mediaStoreId}_$i"
                            encodingHashMap[mapKey] = faceNetModel.getEmbedding(faceBitmap)
                            val alignmentValue = face.headEulerAngleY.absoluteValue + face.headEulerAngleX.absoluteValue + face.headEulerAngleZ.absoluteValue
                            faceAlignmentMap[mapKey] = alignmentValue
                            FileUtil.savePhotoToExternalStorageCache(applicationContext, faceBitmap, "$FACES_DIR/$mapKey.png")
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
            clusterMap.forEach { (clusterId, files) ->
                val mostAlignedFace = faceAlignmentMap.asSequence().filter { files.contains(it.key) }.minByOrNull { it.value }
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
                        val mediaStoreIds = files.map { it.split("_")[0].toLong() }
                        galleryDatabase.mediaItemDao.updateClusterByIds(clusterId, mediaStoreIds)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        Log.d(LOG_TAG, "error parsing medias store ids from string to long")
                    }
                }
            }
        }
        return true
    }

    private fun updateCluster(newItemsIds: LongArray, deletedItems: LongArray) {

    }

    private fun saveEncodingsToFile(encodingHashMap: HashMap<String, FloatArray>) {
        try {
            applicationContext.openFileOutput("encoding", Context.MODE_PRIVATE).use {
                val objectOutputStream = ObjectOutputStream(it)
                objectOutputStream.writeObject(encodingHashMap)
                objectOutputStream.flush()
                objectOutputStream.close()
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(LOG_TAG, "error saving encoding hash map to file")
        }
    }

    private fun readEncodingsToFile(): HashMap<String, FloatArray>? {
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
            Log.d(LOG_TAG, "error saving encoding hash map to file")
            return null
        }
    }
}