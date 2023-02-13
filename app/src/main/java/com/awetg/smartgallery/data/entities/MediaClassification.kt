package com.awetg.smartgallery.data.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "classification", indices = [(Index(value = ["name"], unique = true))])
data class MediaClassification(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "media_item_ids") var mediaItemIds: List<Long>,
    @ColumnInfo(name = "type") var type: Int,
) {
    companion object {
        const val CLASSIFICATION_TYPE_CLUSTER = 1
        const val CLASSIFICATION_TYPE_OBJECT_DETECTION = 2
        const val CLASSIFICATION_TYPE_IMAGE_CLASS = 3
    }
}