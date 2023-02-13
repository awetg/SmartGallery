package com.awetg.smartgallery.domain.use_case

import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.repository.MediaClassificationRepository
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import kotlinx.coroutines.flow.Flow


// GET
class GetAllMediaClassificationUseCase(private val repository: MediaClassificationRepository) {
    operator fun invoke():  Flow<List<MediaClassification>> {
        return repository.getAllMediaClassification()
    }
}

class GetAllMediaClassificationByIdUseCase(private val repository: MediaClassificationRepository) {
    operator fun invoke(id: Long):  Flow<MediaClassification> {
        return repository.getAllMediaClassificationById(id)
    }
}

// ADD
class AddMediaItemsUseCase (private val repository: MediaItemRepository) {
    operator fun invoke(mediaItems: ArrayList<MediaItem>) {
        repository.insertAll(mediaItems)
    }
}