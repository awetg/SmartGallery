package com.awetg.smartgallery.data.repository

import com.awetg.smartgallery.data.data.MediaItemDao
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import kotlinx.coroutines.flow.Flow

class MediaItemRepositoryImpl(private val dao: MediaItemDao): MediaItemRepository {
    override fun getMediaItems(): Flow<List<MediaItem>> {
        return dao.getMediaItems()
    }

    override fun getMediaItemsByModifiedAt(): Flow<List<MediaItem>> {
        return dao.getMediaItemsByModifiedAt()
    }

    override fun getMediaItemsByPath(path: String): Flow<List<MediaItem>> {
        return dao.getMediaItemsByPath(path)
    }

    override fun insert(mediaItem: MediaItem) {
        return dao.insert(mediaItem)
    }

    override fun insertAll(mediaItem: List<MediaItem>) {
        return dao.insertAll(mediaItem)
    }

    override fun deleteMedia(mediaItem: MediaItem) {
        return dao.deleteMedia(mediaItem)
    }

    override fun deleteMediaItemsByPath(path: String) {
        return dao.deleteMediaItemsByPath(path)
    }

    override fun deleteAll() {
        return dao.deleteAll()
    }

    override fun updateClusterByIds(clusterId: Int, ids: List<Long>) {
        dao.updateClusterByIds(clusterId, ids)
    }

    override fun getMediaItemsByIds(ids: List<Long>): Flow<List<MediaItem>> {
        return dao.getMediaItemsByIds(ids)
    }

}