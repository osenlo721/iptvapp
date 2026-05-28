package com.iptvapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val category: String = "Otros",
    val epgId: String? = null,
    val groupTitle: String? = null,
    val isFavorite: Boolean = false,
    val streamType: StreamType = StreamType.HLS
)

enum class StreamType { HLS, DASH, MP4, RTMP, UNKNOWN }
