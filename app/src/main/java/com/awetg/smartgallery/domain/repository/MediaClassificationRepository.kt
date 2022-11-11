package com.awetg.smartgallery.domain.repository

import com.awetg.smartgallery.data.entities.MediaClassification
import kotlinx.coroutines.flow.Flow

interface MediaClassificationRepository {
    fun getAllMediaClassification(): Flow<List<MediaClassification>>
    fun insertAll(mediaClassifications: List<MediaClassification>)
}