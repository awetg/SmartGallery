package com.awetg.smartgallery.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.data.repository.MediaClassificationRepositoryImpl
import com.awetg.smartgallery.data.repository.MediaItemRepositoryImpl
import com.awetg.smartgallery.domain.repository.MediaClassificationRepository
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import com.awetg.smartgallery.domain.use_case.*
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
import com.awetg.smartgallery.ui.screens.searchScreen.SearchViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGalleryDatabase(application: Application): GalleryDatabase {
        return Room.databaseBuilder(
            application,
            GalleryDatabase::class.java,
            GalleryDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideMediaItemRepository(db: GalleryDatabase): MediaItemRepository {
        return  MediaItemRepositoryImpl(db.mediaItemDao)
    }

    @Provides
    @Singleton
    fun provideMediaClassificationRepository(db: GalleryDatabase): MediaClassificationRepository {
        return  MediaClassificationRepositoryImpl(db.mediaClassificationDao)
    }

    @Provides
    @Singleton
    fun providePhotosUseCases(repository: MediaItemRepository): PhotosUseCases {
        return PhotosUseCases(
            getMediaItems = GetMediaItemsUseCase(repository),
            getMediaItemsByModifiedAt = GetMediaItemsByModifiedAtUseCase(repository),
            addMediaItems = AddMediaItemsUseCase(repository),
            deleteAllMediaItems = DeleteAllMediaItemsUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideSearchUseCases(classificationRepository: MediaClassificationRepository, mediaRepository: MediaItemRepository): SearchUseCases {
        return SearchUseCases(
            getAllMediaClassification = GetAllMediaClassificationUseCase(classificationRepository),
            addMediaClassifications = AddMediaClassificationUseCase(classificationRepository),
            getMediaItemsByIds = GetMediaItemsByIdsUseCase(mediaRepository)
        )
    }

    @Provides
    @Singleton
    fun providePhotosViewModel(photosUseCases: PhotosUseCases, sharedPreferenceUtil: SharedPreferenceUtil): PhotosViewModel {
        return PhotosViewModel(photosUseCases, sharedPreferenceUtil)
    }

    @Provides
    @Singleton
    fun provideSearchViewModel(searchUseCases: SearchUseCases, sharedPreferenceUtil: SharedPreferenceUtil): SearchViewModel {
        return SearchViewModel(searchUseCases, sharedPreferenceUtil)
    }

    @Singleton
    @Provides
    fun provideSharedPreferenceUtil(@ApplicationContext context: Context): SharedPreferenceUtil {
        return SharedPreferenceUtil(context)
    }
}