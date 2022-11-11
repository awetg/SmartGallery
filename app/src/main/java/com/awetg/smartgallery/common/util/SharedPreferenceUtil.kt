package com.awetg.smartgallery.common.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceUtil @Inject constructor(@ApplicationContext context : Context) {
    val prefs: SharedPreferences = context.getSharedPreferences(APP_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    companion object {
        const val APP_PREFERENCE_FILE_KEY = "smart_app_preference"

        const val FIRST_APP_RUN_KEY = "first_run"

        const val MEDIA_STORE_FIRST_SCAN_KEY = "first_scan"
        const val MEDIA_STORE_MEDIA_COUNT = "last_scan"
        const val MEDIA_STORE_VERSION_KEY = "media_version"
        const val MEDIA_STORE_GENERATION_KEY = "media_generation"

        const val CLUSTER_JOB_COMPLETE = "cluster_job_complete"

        const val CLASSIFICATION_JOB_COMPLETE = "classification_job_complete"

    }

    fun saveBoolean(keyName: String, value: Boolean) {
        prefs.edit().putBoolean(keyName, value).apply()
    }

    private fun saveInt(keyName: String, value: Int) {
        prefs.edit().putInt(keyName, value).apply()
    }

    private fun saveLong(keyName: String, value: Long) {
        prefs.edit().putLong(keyName, value).apply()
    }

    private fun saveString(keyName: String, value: String) {
        prefs.edit().putString(keyName, value).apply()
    }

    fun addMediaStoreVersion(context: Context, version: String?) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
            val currentVersion = version ?: MediaStore.getVersion(context)
            saveString(MEDIA_STORE_VERSION_KEY, currentVersion)
        }
    }

    fun addMediaStoreGeneration(context: Context, generation: Long?) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
            val currentGeneration = generation ?: MediaStore.getGeneration(context, "external")
            saveLong(MEDIA_STORE_GENERATION_KEY, currentGeneration)
        }
    }

    fun updateMediaCount(newCount: Int): Boolean {
        val savedCount = prefs.getInt(MEDIA_STORE_MEDIA_COUNT, 0)
        if (savedCount != newCount) {
            saveInt(MEDIA_STORE_MEDIA_COUNT, newCount)
            return true
        }
        return false
    }
}