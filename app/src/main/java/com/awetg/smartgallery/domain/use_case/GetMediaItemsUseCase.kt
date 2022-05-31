package com.awetg.smartgallery.domain.use_case



import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import kotlinx.coroutines.flow.Flow

class GetMediaItemsUseCase(private val repository: MediaItemRepository) {
    operator fun invoke(): Flow<List<MediaItem>> {
        return repository.getMediaItems()
    }
}

class DeleteAllMediaItemsUseCase(private val repository: MediaItemRepository) {
    operator fun invoke() {
        return repository.deleteAll()
    }
}