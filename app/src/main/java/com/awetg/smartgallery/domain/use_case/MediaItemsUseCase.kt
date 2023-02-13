package com.awetg.smartgallery.domain.use_case



import com.awetg.smartgallery.data.entities.MediaClassification
import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.repository.MediaClassificationRepository
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import kotlinx.coroutines.flow.Flow


// GET
class GetMediaItemsUseCase(private val repository: MediaItemRepository) {
    operator fun invoke(): Flow<List<MediaItem>> {
        return repository.getMediaItems()
    }
}

class GetMediaItemsByModifiedAtUseCase(private val repository: MediaItemRepository) {
    operator fun invoke(): Flow<List<MediaItem>> {
        return repository.getMediaItemsByModifiedAt()
    }
}

class DeleteAllMediaItemsUseCase(private val repository: MediaItemRepository) {
    operator fun invoke() {
        return repository.deleteAll()
    }
}

class GetMediaItemsByIdsUseCase(private val repository: MediaItemRepository) {
    operator fun invoke(ids: List<Long>): Flow<List<MediaItem>> {
        return repository.getMediaItemsByIds(ids)
    }
}

// ADD
class AddMediaClassificationUseCase(private val repository: MediaClassificationRepository) {
    operator fun invoke(mediaClassifications: List<MediaClassification>) {
        repository.insertAll(mediaClassifications)
    }
}