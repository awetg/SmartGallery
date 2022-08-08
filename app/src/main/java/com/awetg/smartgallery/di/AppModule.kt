package com.awetg.smartgallery.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.awetg.smartgallery.common.util.SharedPreferenceUtil
import com.awetg.smartgallery.data.data.GalleryDatabase
import com.awetg.smartgallery.data.repository.MediaItemRepositoryImpl
import com.awetg.smartgallery.domain.repository.MediaItemRepository
import com.awetg.smartgallery.domain.use_case.AddMediaItemsUseCase
import com.awetg.smartgallery.domain.use_case.DeleteAllMediaItemsUseCase
import com.awetg.smartgallery.domain.use_case.GetMediaItemsUseCase
import com.awetg.smartgallery.domain.use_case.PhotosUseCases
import com.awetg.smartgallery.ui.screens.photosScreen.PhotosViewModel
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
    fun ProvideGalleryDatabase(application: Application): GalleryDatabase {
        return Room.databaseBuilder(
            application,
            GalleryDatabase::class.java,
            GalleryDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun ProvideMediaItemRepository(db: GalleryDatabase): MediaItemRepository {
        return  MediaItemRepositoryImpl(db.mediaItemDao)
    }

    @Provides
    @Singleton
    fun providePhotosUseCases(repository: MediaItemRepository): PhotosUseCases {
        return PhotosUseCases(
            getMediaItems = GetMediaItemsUseCase(repository),
            addMediaItems = AddMediaItemsUseCase(repository),
            deleteAllMediaItems = DeleteAllMediaItemsUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providePhotosViewModel(photosUseCases: PhotosUseCases, sharedPreferenceUtil: SharedPreferenceUtil): PhotosViewModel {
        return PhotosViewModel(photosUseCases, sharedPreferenceUtil)
    }

    @Singleton
    @Provides
    fun provideSharedPreferenceUtil(@ApplicationContext context: Context): SharedPreferenceUtil {
        return SharedPreferenceUtil(context)
    }
}