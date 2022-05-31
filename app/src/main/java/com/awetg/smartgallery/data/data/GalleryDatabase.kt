package com.awetg.smartgallery.data.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.awetg.smartgallery.data.entities.Converters
import com.awetg.smartgallery.data.entities.MediaItem

@Database(entities = [MediaItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class GalleryDatabase: RoomDatabase() {
    abstract val mediaItemDao: MediaItemDao
    companion object {
        const val DATABASE_NAME = "gallery.db"
    }
}