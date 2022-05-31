package com.awetg.smartgallery.domain.use_case

import com.awetg.smartgallery.data.entities.MediaItem
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import kotlinx.coroutines.flow.Flow

class AddMediaItemsUseCase (private val repository: MediaItemRepository) {
    operator fun invoke(mediaItems: ArrayList<MediaItem>) {
        repository.insertAll(mediaItems)
    }
}