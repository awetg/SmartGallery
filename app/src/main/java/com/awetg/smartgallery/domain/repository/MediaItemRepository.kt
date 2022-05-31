package com.awetg.smartgallery.domain.repository

import com.awetg.smartgallery.data.entities.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaItemRepository {
    fun getMediaItems(): Flow<List<MediaItem>>
    fun getMediaItemsByPath(path: String): Flow<List<MediaItem>>
    fun insert(mediaItem: MediaItem)
    fun insertAll(mediaItem: List<MediaItem>)
    fun deleteMedia(mediaItem: MediaItem)
    fun deleteMediaItemsByPath(path: String)
    fun deleteAll()
}