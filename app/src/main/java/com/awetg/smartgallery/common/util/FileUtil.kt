package com.awetg.smartgallery.common.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.awetg.smartgallery.common.CLUSTER_DIR
import com.awetg.smartgallery.common.LOG_TAG
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun createDirInExternalStorageCache(context: Context, dirName: String): Boolean {
        return try {
            val externalCacheFile = File(context.externalCacheDir, dirName)
            if (!externalCacheFile.exists()) {
                externalCacheFile.mkdir()
            }
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "could not create directory in external storage cache")
            e.printStackTrace()
            false
        }
    }

    fun savePhotoToExternalStorageCache(context: Context, bitmap: Bitmap, path: String): Boolean {
        return try {
            val f = File(context.externalCacheDir, path)
            val outputStream = FileOutputStream(f)
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)) {
                throw IOException("can't save bitmap")
            }
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error saving photo")
            e.printStackTrace()
            false
        }
    }

    fun createDirInExternalStorage(context: Context, dirName: String): Boolean {
        return try {
            val externalCacheFile = File(context.getExternalFilesDir(null), dirName)
            if (!externalCacheFile.exists()) {
                externalCacheFile.mkdir()
            }
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "could not create directory in external storage cache")
            e.printStackTrace()
            false
        }
    }

    fun savePhotoToExternalStorage(context: Context, bitmap: Bitmap, path: String): Boolean {
        return try {
            val f = File(context.getExternalFilesDir(null), path)
            val outputStream = FileOutputStream(f)
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)) {
                throw IOException("can't save bitmap")
            }
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error saving photo")
            e.printStackTrace()
            false
        }
    }

    fun getClusterPhoto(context: Context, clusterId: Int): File? {
        return try {
            val file = File(context.getExternalFilesDir(null), "$CLUSTER_DIR/$clusterId.png")
            if (file.exists()) file else null
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error getting cluster photo")
            e.printStackTrace()
            null
        }
    }


    private fun saveStringToFile(context: Context,data: String) {
        try {
            val f = File(context.getExternalFilesDir(null), "matrix.txt")
            f.writeText(data)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "error saving string to file")
            e.printStackTrace()
        }
    }
}