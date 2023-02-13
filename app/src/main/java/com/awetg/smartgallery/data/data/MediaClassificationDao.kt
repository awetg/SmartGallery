package com.awetg.smartgallery.data.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.awetg.smartgallery.data.entities.MediaClassification
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaClassificationDao {
    @Query("SELECT * FROM classification")
    fun getAllMediaClassification(): Flow<List<MediaClassification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(mediaClassifications: List<MediaClassification>)

    @Query("SELECT * FROM classification where id = :id")
    fun getAllMediaClassificationById(id: Long): Flow<MediaClassification>
}