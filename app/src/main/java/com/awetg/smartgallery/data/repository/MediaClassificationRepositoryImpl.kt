package com.awetg.smartgallery.data.repository

import com.awetg.smartgallery.data.data.MediaClassificationDao
import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.domain.repository.MediaClassificationRepository
import kotlinx.coroutines.flow.Flow

class MediaClassificationRepositoryImpl(private val dao: MediaClassificationDao): MediaClassificationRepository {
    override fun getAllMediaClassification(): Flow<List<MediaClassification>> {
        return dao.getAllMediaClassification()
    }

    override fun insertAll(mediaClassifications: List<MediaClassification>) {
        dao.insertAll(mediaClassifications)
    }

    override fun getAllMediaClassificationById(id: Long): Flow<MediaClassification> {
        return dao.getAllMediaClassificationById(id)
    }
}