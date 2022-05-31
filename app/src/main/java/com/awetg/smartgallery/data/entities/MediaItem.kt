package com.awetg.smartgallery.data.entities

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.*

@Entity(tableName = "media", indices = [(Index(value = ["full_path"], unique = true))])
data class MediaItem(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "filename") var name: String,
    @ColumnInfo(name = "parent_path") var parentPath: String,
    @ColumnInfo(name = "full_path") var fullPath: String,
    @ColumnInfo(name = "modified_at") var modifiedAt: Long,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "deleted_at") var deletedAt: Long,
    @ColumnInfo(name = "size") var size: Long,
    @ColumnInfo(name = "media_store_id") var mediaStoreId: Long,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "uri") var uri: Uri,
    @ColumnInfo(name = "height") var height: Int,
    @ColumnInfo(name = "width") var width: Int,
) {
    companion object {
        // media constants
        const val MEDIA_TYPE_IMAGE = 1
        const val MEDIA_TYPE_GIF = 2
        const val MEDIA_TYPE_VIDEO = 3

        //        val MediaTypes = listOf(MEDIA_TYPE_IMAGE, MEDIA_TYPE_GIF, MEDIA_TYPE_VIDEO)

    }
}


