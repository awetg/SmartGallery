package com.awetg.smartgallery.data.entities

import android.net.Uri
import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun uriToString(value: Uri): String {
        return value.toString()
    }

    @TypeConverter
    fun stringToUri(value: String): Uri {
        return Uri.parse(value)
    }
}