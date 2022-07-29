package com.awetg.smartgallery

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import com.awetg.smartgallery.common.FOREGROUND_WORK_NOTIFICATION_CHANNEL_ID
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.services.MyWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartGalleryApp: Application(), Configuration.Provider {
    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    @Inject
    lateinit var galleryDatabase: GalleryDatabase

    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory

    override fun onCreate() {
        super.onCreate()
        val first = sharedPreferenceUtil.prefs.getBoolean(SharedPreferenceUtil.FIRST_APP_RUN_KEY, true)
        if (first) {
            createNotificationChannel(
                FOREGROUND_WORK_NOTIFICATION_CHANNEL_ID,
                "Media Scan",
                "This channel is used to notify media scanning",
                NotificationManager.IMPORTANCE_LOW
            )
            sharedPreferenceUtil.saveBoolean(SharedPreferenceUtil.FIRST_APP_RUN_KEY, false)
        }
    }

    // Creates notification channel with given params
    private fun createNotificationChannel(channelId: String, channelName: String, channelDescription: String, priority: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName,priority)
            channel.description = channelDescription
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.DEBUG)
        .setWorkerFactory(myWorkerFactory)
        .build()
}