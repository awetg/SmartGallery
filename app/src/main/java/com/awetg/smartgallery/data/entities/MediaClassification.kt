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
) {
}