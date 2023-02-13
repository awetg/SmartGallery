package com.awetg.smartgallery.domain.use_case

data class SearchUseCases(
    val getAllMediaClassification: GetAllMediaClassificationUseCase,
    val addMediaClassifications: AddMediaClassificationUseCase,
    val getMediaItemsByIds: GetMediaItemsByIdsUseCase,
    val getAllMediaClassificationByIdUseCase: GetAllMediaClassificationByIdUseCase,
)