package com.awetg.smartgallery.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.work.*
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.services.workers.FaceClusterWorker
import com.awetg.smartgallery.services.workers.MediaScanWorker
import com.awetg.smartgallery.services.workers.YoloObjectDetectionWorker
import com.awetg.smartgallery.ui.components.BottomNavController
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import com.awetg.smartgallery.ui.screens.searchScreen.SearchViewModel
import com.awetg.smartgallery.ui.theme.SmartGalleryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var photosViewModel: PhotosViewModel
    @Inject
    lateinit var searchViewModel: SearchViewModel
    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    private var permissionsGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if(results.values.any { !it }) {
                finishAffinity()
            }
            checkMediaScan()
        }
        if (permissionsGranted) checkMediaScan() else requestPermission()
        setContent { AppView() }
    }

    override fun onResume() {
        super.onResume()
        if (permissionsGranted) checkMediaScan()
    }


    private fun saveMediaStoreGeneration() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
            val currentGeneration = MediaStore.getGeneration(this, "external")
            sharedPreferenceUtil.addMediaStoreGeneration(this, currentGeneration)
        }
    }

    private fun getMediaScanType(): String {
        val firsScan = sharedPreferenceUtil.prefs.getBoolean(SharedPreferenceUtil.MEDIA_STORE_FIRST_SCAN_KEY, true)
        val currentVersion = MediaStore.getVersion(this)

        // if media scan is never run before do full sync
        if (firsScan) {
            sharedPreferenceUtil.saveBoolean(SharedPreferenceUtil.MEDIA_STORE_FIRST_SCAN_KEY, false)
            sharedPreferenceUtil.addMediaStoreVersion(this, currentVersion)
            saveMediaStoreGeneration()
            return MEDIA_SCAN_TYPE_SYNC
        }


        val savedVersion = sharedPreferenceUtil.prefs.getString(SharedPreferenceUtil.MEDIA_STORE_VERSION_KEY, "")
        // if media store version is changed do full sync
        if(savedVersion!!.isEmpty()) {
            sharedPreferenceUtil.addMediaStoreVersion(this, currentVersion)
            saveMediaStoreGeneration()
            return MEDIA_SCAN_TYPE_SYNC
        }

        // if media store version is changed do a re-sync (same as full sync with removing old data)
        if (savedVersion != currentVersion) {
            sharedPreferenceUtil.addMediaStoreVersion(this, currentVersion)
            sharedPreferenceUtil.addMediaStoreGeneration(this, null)
            return MEDIA_SCAN_TYPE_RE_SYNC
        }

        // if SDK >= 30 only update media database when the media store generation is changed (update only when necessary)
        return if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
            val savedGeneration = sharedPreferenceUtil.prefs.getLong(SharedPreferenceUtil.MEDIA_STORE_GENERATION_KEY, 0L)
            val currentGeneration = MediaStore.getGeneration(this, "external")

            // if media store generation was never saved for some reason, do re-sync
            if(savedGeneration == 0L) {
                sharedPreferenceUtil.addMediaStoreGeneration(this, currentGeneration)
                return MEDIA_SCAN_TYPE_RE_SYNC
            }

            // if media store generation changed update database with changes
            if (savedGeneration != currentGeneration) {
                sharedPreferenceUtil.addMediaStoreGeneration(this, currentGeneration)
                MEDIA_SCAN_TYPE_UPDATE
            }
            else ""

        } else {
            // if SDK < 30 update always (until better method is found to detect media store changes)
            MEDIA_SCAN_TYPE_UPDATE
        }
    }

    private fun checkMediaScan() {
        when(getMediaScanType()) {
            MEDIA_SCAN_TYPE_SYNC -> {
                val newJobData = JobData(MEDIA_SCAN_TYPE_SYNC, MediaScanWorker::class.java)
                enqueueWorker(newJobData, mediaScanObserver)
            }
            MEDIA_SCAN_TYPE_RE_SYNC -> {
                val newJobData = JobData(MEDIA_SCAN_TYPE_SYNC, MediaScanWorker::class.java)
                enqueueWorker(newJobData, mediaScanObserver)
            }
            MEDIA_SCAN_TYPE_UPDATE -> {
                val lastCount = sharedPreferenceUtil.prefs.getInt(SharedPreferenceUtil.MEDIA_STORE_MEDIA_COUNT, 0)
                val newJobData = JobData(MEDIA_SCAN_TYPE_SYNC, MediaScanWorker::class.java, lastCount =  lastCount)
                enqueueWorker(newJobData, mediaScanObserver)
            }
        }
    }

    private fun enqueueWorker(jobData: JobData, observerFunc: (workInfo: WorkInfo, jobData: JobData) -> Unit) {
        Log.d(LOG_TAG, "enqueue job type: ${jobData.jobType}, class: ${jobData.worker}")
        val dataBuilder = Data.Builder()
            .putString(DATA_INPUT_KEY_JOB_TYPE, jobData.jobType)

        if (jobData.jobType == JOB_TYPE_UPDATE) {
            jobData.newItemsIds?.also {  dataBuilder.putLongArray(DATA_KEY_NEW_MEDIAS, it) }
            jobData.deletedItemsIds?.also { dataBuilder.putLongArray(DATA_KEY_DELETED_MEDIAS, it) }
        }
        if (jobData.lastCount !== null) {
            dataBuilder.putInt(DATA_INPUT_KEY_MEDIA_COUNT, jobData.lastCount)
        }
        val data = dataBuilder.build()
        val workerRequest: WorkRequest =
            OneTimeWorkRequest.Builder(jobData.worker)
                .setInputData(data)
                .build()
        WorkManager.getInstance(this).enqueue(workerRequest)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workerRequest.id)
            .observe(this) { workInfo -> workInfo?.also { observerFunc(workInfo, jobData) }}
    }

    private val mediaScanObserver = {workInfo: WorkInfo, jobData: JobData ->
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                Log.d(LOG_TAG, "Media scan succeeded")
                val newCount = workInfo.outputData.getInt(
                    DATA_OUTPUT_KEY_MEDIA_COUNT, 0
                )
                sharedPreferenceUtil.updateMediaCount(newCount)
                if (jobData.jobType == MEDIA_SCAN_TYPE_UPDATE) {
                    val newItemsIds = workInfo.outputData.getLongArray(
                        DATA_OUTPUT_KEY_MEDIA_COUNT
                    )
                    val deletedItemsIds = workInfo.outputData.getLongArray(
                        DATA_OUTPUT_KEY_MEDIA_COUNT
                    )
                    val newJobData = JobData(JOB_TYPE_UPDATE, YoloObjectDetectionWorker::class.java, newItemsIds, deletedItemsIds)
                    enqueueWorker(newJobData, objectDetectorObserver)
                } else {
                    val newJobData = jobData.copy(jobType = JOB_TYPE_ALL, worker = YoloObjectDetectionWorker::class.java)
                    enqueueWorker(newJobData, objectDetectorObserver)
                }
            }
            WorkInfo.State.FAILED -> Log.d(LOG_TAG, "Media scan failed")
            WorkInfo.State.CANCELLED -> Log.d(LOG_TAG, "Media scan cancelled")
            else ->
                Log.d(LOG_TAG, "Running Media Scan")
        }
        if (workInfo.state.isFinished) {
            Log.d(LOG_TAG, "Media scan finished")
            photosViewModel.getMediaItemsByModifiedAt()
        }
    }

    private val objectDetectorObserver = { workInfo: WorkInfo, jobData: JobData ->
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                Log.d(LOG_TAG, "Object detector succeeded")
                sharedPreferenceUtil.saveBoolean(SharedPreferenceUtil.CLASSIFICATION_JOB_COMPLETE, true)
                searchViewModel.updateClassificationState()
                val newJobData = jobData.copy(worker = FaceClusterWorker::class.java)
                enqueueWorker(newJobData, faceClusterObserver)
            }
            WorkInfo.State.FAILED -> Log.d(LOG_TAG, "Object detector failed")
            WorkInfo.State.CANCELLED -> Log.d(LOG_TAG, "Object detector cancelled")
            else ->
                Log.d(LOG_TAG, "Running media object detection")
        }
        if (workInfo.state.isFinished) {
            Log.d(LOG_TAG, "Object detector finished")
        }
    }

    private val faceClusterObserver = { workInfo: WorkInfo, _: JobData ->
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                Log.d(LOG_TAG, "Face cluster succeeded")
                sharedPreferenceUtil.saveBoolean(SharedPreferenceUtil.CLUSTER_JOB_COMPLETE, true)
                searchViewModel.updateClassificationState()
            }
            WorkInfo.State.FAILED -> Log.d(LOG_TAG, "Face cluster failed")
            WorkInfo.State.CANCELLED -> Log.d(LOG_TAG, "Face cluster cancelled")
            else ->
                Log.d(LOG_TAG, "Running face cluster")
        }
        if (workInfo.state.isFinished) { Log.d(LOG_TAG, "Face cluster finished") }
    }

    private val classifierObserver = { workInfo: WorkInfo, jobData: JobData ->
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                Log.d(LOG_TAG, "Media classification succeeded")
                sharedPreferenceUtil.saveBoolean(SharedPreferenceUtil.CLASSIFICATION_JOB_COMPLETE, true)
                searchViewModel.updateClassificationState()
//                val newJobData = jobData.copy(worker = FaceClusterWorker::class.java)
//                enqueueWorker(newJobData, faceClusterObserver)
            }
            WorkInfo.State.FAILED -> Log.d(LOG_TAG, "Media classification failed")
            WorkInfo.State.CANCELLED -> Log.d(LOG_TAG, "Media classification cancelled")
            else ->
                Log.d(LOG_TAG, "Running media classification")
        }
        if (workInfo.state.isFinished) {
            Log.d(LOG_TAG, "Media classification finished")
        }
    }


    private fun requestPermission() {
        val requiredPermissions = mutableListOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            requiredPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= 28) {
            requiredPermissions.add(android.Manifest.permission.FOREGROUND_SERVICE)
        }
        fun isPermissionAvailable(permission: String): Boolean = this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        val missingPermissions = requiredPermissions.filter { isPermissionAvailable(it) }
        if (missingPermissions.isEmpty()) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

data class JobData(
    val jobType: String,
    val worker: Class<out ListenableWorker>,
    val newItemsIds: LongArray? = null,
    val deletedItemsIds: LongArray? = null,
    val lastCount: Int? = null,
    )


@Composable
fun AppView() {
    SmartGalleryTheme {
        BottomNavController()
    }
}