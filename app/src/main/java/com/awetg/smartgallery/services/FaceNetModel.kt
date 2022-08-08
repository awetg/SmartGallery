package com.awetg.smartgallery.services

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class FaceNetModel(private val context: Context, assetName: String) {
    private val nnApiDelegate by lazy {
        NnApiDelegate()
    }

    private val interpreter by lazy {
        Interpreter(
            FileUtil.loadMappedFile(context, assetName),
            Interpreter.Options().addDelegate(nnApiDelegate)
        )
    }

    private val imageTensorProcessor = ImageProcessor.Builder()
        .add(ResizeOp(160, 160, ResizeOp.ResizeMethod.BILINEAR))
        .add(StandardizeOperator())
        .build()

    class StandardizeOperator : TensorOperator {
        override fun apply(p0: TensorBuffer?): TensorBuffer {
            val pixels = p0!!.floatArray
            val mean = pixels.average().toFloat()
            var std = sqrt(pixels.map { pi -> (pi - mean).pow(2) }.sum() / pixels.size.toFloat())
            std = max(std, 1f / sqrt(pixels.size.toFloat()))
            for (i in pixels.indices) {
                pixels[i] = (pixels[i] - mean) / std
            }
            val output = TensorBufferFloat.createFixedSize(p0.shape, DataType.FLOAT32)
            output.loadArray(pixels)
            return output
        }
    }

    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        return if (bitmap.config.name == "ARGB_8888") {
            imageTensorProcessor.process(TensorImage.fromBitmap(bitmap)).buffer

        } else {
            val bitmapARGB_8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            imageTensorProcessor.process(TensorImage.fromBitmap(bitmapARGB_8888)).buffer

        }
    }

    private fun runFaceNet(inputs: Any): Array<FloatArray> {
        val faceNetModelOutputs = Array(1) { FloatArray(128) }
        interpreter.run(inputs, faceNetModelOutputs)
        return faceNetModelOutputs

    }

    suspend fun getEmbedding(bitmap: Bitmap): FloatArray {
        return withContext( Dispatchers.Default ) {
            return@withContext runFaceNet( convertBitmapToBuffer( bitmap ))[0]
        }
    }
}