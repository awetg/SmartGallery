package com.awetg.smartgallery.services.mlmodels

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream


class PytorchYolo5Model(private val context: Context, assetName: String) {

    private val module by lazy {
        LiteModuleLoader.load(getAbsolutePathOfAssetFile(context, assetName))
    }

    fun detectObjects(bitmap: Bitmap): List<String> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val inputTensor =
            TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, NO_MEAN_RGB, NO_STD_RGB)
        val forwardOutput = module.forward(IValue.from(inputTensor))
        val outputTuple = forwardOutput.toTuple()
        val outputTensor = outputTuple[0].toTensor()
        val outputs = outputTensor.dataAsFloatArray
        return outputsToNMSPredictions(outputs)
    }

    /* since we can't get the absolute path to asset file included in the APK
    we copy it first to files dir then get absolute path */
    private fun getAbsolutePathOfAssetFile(context: Context, assetName: String): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            return file.absolutePath
        }
    }

    private fun outputsToNMSPredictions(outputs: FloatArray): List<String> {
        val results = mutableListOf<String>()
        for (i in 0 until mOutputRow) {
            // if score is greater than threshold
            if (outputs[i * mOutputColumn + 4] > mThreshold) {
                var max = outputs[i * mOutputColumn + 5]
                var cls = 0
                for (j in 0 until mOutputColumn - 5) {
                    if (outputs[i * mOutputColumn + 5 + j] > max) {
                        max = outputs[i * mOutputColumn + 5 + j]
                        cls = j
                    }
                }
                results.add(YOLO5_CLASSES[cls])
            }
        }
        // we don't care about bounding box of the detected objects for our feature,
        // it will only be used fo search purpose
        // we will filter out unique classes
        return results.distinct()
    }

    companion object {
        private const val inputWidth = 640
        private const val inputHeight = 640

        private val NO_MEAN_RGB = floatArrayOf(0.0f, 0.0f, 0.0f)
        private val NO_STD_RGB = floatArrayOf(1.0f, 1.0f, 1.0f)

        // model output is of size 25200*(num_of_class+5)
        // as decided by the YOLOv5 model for input image of size 640*640
        private const val mOutputRow = 25200

        // left, top, right, bottom, score and 80 class probability
        private const val mOutputColumn = 85

        private const val mThreshold = 0.55f // score above which a detection is generated

        private val YOLO5_CLASSES = listOf(
            "person",
            "bicycle",
            "car",
            "motorcycle",
            "airplane",
            "bus",
            "train",
            "truck",
            "boat",
            "traffic light",
            "fire hydrant",
            "stop sign",
            "parking meter",
            "bench",
            "bird",
            "cat",
            "dog",
            "horse",
            "sheep",
            "cow",
            "elephant",
            "bear",
            "zebra",
            "giraffe",
            "backpack",
            "umbrella",
            "handbag",
            "tie",
            "suitcase",
            "frisbee",
            "skis",
            "snowboard",
            "sports ball",
            "kite",
            "baseball bat",
            "baseball glove",
            "skateboard",
            "surfboard",
            "tennis racket",
            "bottle",
            "wine glass",
            "cup",
            "fork",
            "knife",
            "spoon",
            "bowl",
            "banana",
            "apple",
            "sandwich",
            "orange",
            "broccoli",
            "carrot",
            "hot dog",
            "pizza",
            "donut",
            "cake",
            "chair",
            "couch",
            "potted plant",
            "bed",
            "dining table",
            "toilet",
            "tv",
            "laptop",
            "mouse",
            "remote",
            "keyboard",
            "cell phone",
            "microwave",
            "oven",
            "toaster",
            "sink",
            "refrigerator",
            "book",
            "clock",
            "vase",
            "scissors",
            "teddy bear",
            "hair drier",
            "toothbrush",
        )
    }
}