package com.awetg.smartgallery.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.work.*
import com.awetg.smartgallery.ui.components.BottomNavController
import com.awetg.smartgallery.common.*
import com.awetg.smartgallery.ui.theme.SmartGalleryTheme
import com.awetg.smartgallery.common.util.PermissionUtil
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.services.MediaScanWorker
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModel: PhotosViewModel
    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PermissionUtil.allPermissionGranted(this)) checkMediaScan() else PermissionUtil.requestAllMissingPermissions(this)
        setContent { AppView() }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtil.allPermissionGranted(this)) checkMediaScan()
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

        val onSuccess = { newCount: Int ->
            val updated = sharedPreferenceUtil.updateMediaCount(newCount)
            if (updated) viewModel.reloadMediaItems()
        }
        val onFailure = { }
        val onCancel = { }
        val onFinnish = { }

        when(getMediaScanType()) {
            MEDIA_SCAN_TYPE_SYNC -> {
                val data = Data.Builder()
                    .putString(DATA_INPUT_KEY_WORK_TYPE, MEDIA_SCAN_TYPE_SYNC)
                    .build()
                val mWorkRequest: WorkRequest =
                    OneTimeWorkRequest.Builder(MediaScanWorker::class.java)
                        .setInputData(data)
                        .build()
                enqueueWorker(mWorkRequest, onSuccess, onFailure, onCancel) {
                    onFinnish()
                    viewModel.reloadMediaItems()
                }
            }
            MEDIA_SCAN_TYPE_RE_SYNC -> {
                val data = Data.Builder()
                    .putString(DATA_INPUT_KEY_WORK_TYPE, MEDIA_SCAN_TYPE_RE_SYNC)
                    .build()
                val mWorkRequest: WorkRequest =
                    OneTimeWorkRequest.Builder(MediaScanWorker::class.java)
                        .setInputData(data)
                        .build()
                enqueueWorker(mWorkRequest, onSuccess, onFailure, onCancel) {
                    onFinnish()
                    viewModel.reloadMediaItems()
                }
            }
            MEDIA_SCAN_TYPE_UPDATE -> {
                val lastModifiedAt = sharedPreferenceUtil.prefs.getInt(SharedPreferenceUtil.MEDIA_STORE_MEDIA_COUNT, 0)
                val data = Data.Builder()
                    .putString(DATA_INPUT_KEY_WORK_TYPE, MEDIA_SCAN_TYPE_UPDATE)
                    .putInt(DATA_INPUT_KEY_MEDIA_COUNT, lastModifiedAt)
                    .build()
                val mWorkRequest: WorkRequest =
                    OneTimeWorkRequest.Builder(MediaScanWorker::class.java)
                        .setInputData(data)
                        .build()
                enqueueWorker(mWorkRequest, onSuccess, onFailure, onCancel) {
                    onFinnish()
                }
            }
        }
    }

    private fun enqueueWorker(workerRequest: WorkRequest, onSuccess: (Int) -> Unit, onFailure: () -> Unit, onCancel: () -> Unit, onFinnish: () -> Unit) {
        WorkManager.getInstance(this).enqueue(workerRequest)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workerRequest.id)
            .observe(this) { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val newCount = workInfo.outputData.getInt(
                            DATA_OUTPUT_KEY_MEDIA_COUNT, 0
                        )
                        onSuccess(newCount)
                    }
                    WorkInfo.State.FAILED -> onFailure()
                    WorkInfo.State.CANCELLED -> onCancel()
                    else ->
                        Log.d("smartImagesWorker", "Running Media Scan")
                }
                if (workInfo.state.isFinished) onFinnish()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionUtil.PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkMediaScan()
                } else {
                        PermissionUtil.showDialogAndAsk(
                            this,
                            "",
                            "This permission is needed for the app to function properly",
                            { _, _ -> PermissionUtil.requestAllMissingPermissions(this) }
                        )
                }
            }
        }
    }
}


@Composable
fun AppView() {
    SmartGalleryTheme {
        BottomNavController()
    }
}