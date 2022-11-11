package com.awetg.smartgallery.domain.use_case

import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.domain.repository.MediaClassificationRepository
import kotlinx.coroutines.flow.Flow

class GetAllMediaClassificationUseCase(private val repository: MediaClassificationRepository) {
    operator fun invoke():  Flow<List<MediaClassification>> {
        return repository.getAllMediaClassification()
    }
}