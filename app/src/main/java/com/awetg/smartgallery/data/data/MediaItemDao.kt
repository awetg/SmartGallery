package com.awetg.smartgallery.data.data

import androidx.room.*
import com.awetg.smartgallery.data.entities.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {

    @Query("SELECT * FROM media ORDER BY modified_at DESC")
    fun getMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media WHERE deleted_at =0 AND parent_path = :path COLLATE NOCASE")
    fun getMediaItemsByPath(path: String): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mediaItem: MediaItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(mediaItem: List<MediaItem>)

    @Delete
    fun deleteMedia(mediaItem: MediaItem)

    @Query("DELETE FROM media WHERE full_path = :path COLLATE NOCASE")
    fun deleteMediaItemsByPath(path: String)

    @Delete
    fun deleteMediaItems(mediaItems: List<MediaItem>)

    @Query("DELETE FROM media")
    fun deleteAll()
}