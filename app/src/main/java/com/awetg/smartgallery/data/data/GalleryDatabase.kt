package com.awetg.smartgallery.data.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.awetg.smartgallery.data.entities.Converters
import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.data.entities.MediaItem

@Database(entities = [MediaItem::class, MediaClassification::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GalleryDatabase: RoomDatabase() {
    abstract val mediaItemDao: MediaItemDao
    abstract val mediaClassificationDao: MediaClassificationDao
    companion object {
        const val DATABASE_NAME = "gallery.db"
    }
}