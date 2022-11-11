package com.awetg.smartgallery.domain.use_case

import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.domain.repository.MediaClassificationRepository
import com.awetg.smartgallery.domain.repository.MediaItemRepository

class AddMediaClassificationUseCase(private val repository: MediaClassificationRepository) {
    operator fun invoke(mediaClassifications: List<MediaClassification>) {
        repository.insertAll(mediaClassifications)
    }
}