package com.awetg.smartgallery.services.mlmodels

import android.graphics.Bitmap
import android.util.Log
import com.awetg.smartgallery.common.LOG_TAG
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.lang.IllegalArgumentException

class MLKitFaceDetector {
    private val detector: FaceDetector
    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
        detector = FaceDetection.getClient(options)
    }

    fun detectFaces(inputImage: InputImage): Task<MutableList<Face>> {
        return detector.process(inputImage)
    }

    fun getCroppedFace(bitmap: Bitmap, face: Face): Bitmap? {
        val rect = face.boundingBox
//        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
//        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degree
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
        return try {
            Bitmap.createBitmap( bitmap , left , top , width , height)
        } catch (e: IllegalArgumentException) {
            Log.e(LOG_TAG, "Negative rect")
            e.printStackTrace()
            null
        }
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
                yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                index++
            }
        }
    }
    private fun bitmapToNV21ByteArray(bitmap: Bitmap): ByteArray {
        val argb = IntArray(bitmap.width * bitmap.height )
        bitmap.getPixels(argb, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val yuv = ByteArray(bitmap.height * bitmap.width + 2 * Math.ceil(bitmap.height / 2.0).toInt()
                * Math.ceil(bitmap.width / 2.0).toInt())
        encodeYUV420SP( yuv, argb, bitmap.width, bitmap.height)
        return yuv
    }
}