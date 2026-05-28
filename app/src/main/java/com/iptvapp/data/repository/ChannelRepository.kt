package com.iptvapp.data.repository

import com.iptvapp.data.local.AppDatabase
import com.iptvapp.data.model.Channel
import com.iptvapp.data.parser.M3uParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val db: AppDatabase,
    private val http: OkHttpClient
) {
    private val dao = db.channelDao()

    fun getAllChannels(): Flow<List<Channel>> = dao.getAllChannels()
    fun getChannelsByCategory(cat: String): Flow<List<Channel>> = dao.getChannelsByCategory(cat)
    fun getAllCategories(): Flow<List<String>> = dao.getAllCategories()
    fun searchChannels(query: String): Flow<List<Channel>> = dao.searchChannels(query)
    fun getFavorites(): Flow<List<Channel>> = dao.getFavoriteChannels()

    suspend fun getChannelById(id: String): Channel? = dao.getChannelById(id)

    suspend fun loadFromUrl(url: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder().url(url).build()
            val body = http.newCall(req).execute().use { it.body?.string() }
                ?: error("Respuesta vacía del servidor")
            val channels = M3uParser.parse(body)
            if (channels.isEmpty()) error("No se encontraron canales en la lista")
            dao.deleteAllChannels()
            dao.insertChannels(channels)
            channels.size
        }
    }

    suspend fun loadFromContent(content: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val channels = M3uParser.parse(content)
            if (channels.isEmpty()) error("No se encontraron canales en la lista")
            dao.deleteAllChannels()
            dao.insertChannels(channels)
            channels.size
        }
    }

    suspend fun toggleFavorite(channelId: String, isFav: Boolean) {
        dao.updateFavoriteStatus(channelId, isFav)
    }

    suspend fun clearAll() {
        dao.deleteAllChannels()
    }
}
