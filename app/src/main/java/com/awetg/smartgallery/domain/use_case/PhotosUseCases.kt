package com.awetg.smartgallery.domain.use_case

data class PhotosUseCases(
    val getMediaItems: GetMediaItemsUseCase,
    val getMediaItemsByModifiedAt: GetMediaItemsByModifiedAtUseCase,
    val addMediaItems: AddMediaItemsUseCase,
    val deleteAllMediaItems: DeleteAllMediaItemsUseCase
)
