package com.iptvapp.data.parser

import com.iptvapp.data.model.Channel
import com.iptvapp.data.model.StreamType
import java.util.UUID

object M3uParser {

    /** Returns the url-tvg EPG URL from the #EXTM3U header, if present. */
    fun parseEpgUrl(content: String): String? {
        val header = content.lines().firstOrNull { it.startsWith("#EXTM3U") } ?: return null
        return extractAttr(header, "url-tvg")
    }

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val url = lines.getOrNull(i + 1)?.trim() ?: ""
                if (url.isNotEmpty() && !url.startsWith("#")) {
                    buildChannel(line, url)?.let { channels.add(it) }
                }
                i += 2
            } else {
                i++
            }
        }
        return channels
    }

    private fun buildChannel(extinf: String, url: String): Channel? {
        if (url.isBlank()) return null
        val name = parseName(extinf)
        val logo = extractAttr(extinf, "tvg-logo")
        val epgId = extractAttr(extinf, "tvg-id")
        val group = extractAttr(extinf, "group-title") ?: ""
        val category = normalizeCategory(group)

        return Channel(
            id = epgId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            name = name,
            url = url,
            logoUrl = logo?.takeIf { it.isNotBlank() },
            category = category,
            epgId = epgId?.takeIf { it.isNotBlank() },
            groupTitle = group.takeIf { it.isNotBlank() },
            streamType = detectType(url)
        )
    }

    private fun parseName(extinf: String): String {
        val idx = extinf.lastIndexOf(',')
        return if (idx != -1) extinf.substring(idx + 1).trim() else "Canal desconocido"
    }

    private fun extractAttr(line: String, attr: String): String? =
        Regex("""$attr="([^"]*?)"""").find(line)?.groupValues?.getOrNull(1)

    private fun normalizeCategory(group: String): String = when {
        group.contains("movie", true) || group.contains("pelicul", true) ||
        group.contains("film", true) || group.contains("cine", true) -> "Películas"

        group.contains("sport", true) || group.contains("deport", true) ||
        group.contains("futbol", true) || group.contains("football", true) ||
        group.contains("basket", true) || group.contains("tenis", true) -> "Deportes"

        group.contains("serie", true) || group.contains("show", true) ||
        group.contains("telenovela", true) || group.contains("anime", true) -> "Series"

        group.contains("live", true) || group.contains("vivo", true) ||
        group.contains("news", true) || group.contains("noticia", true) ||
        group.contains("24h", true) -> "En vivo"

        else -> group.takeIf { it.isNotBlank() } ?: "Otros"
    }

    private fun detectType(url: String): StreamType = when {
        url.contains(".m3u8", true) || url.contains("hls", true) -> StreamType.HLS
        url.contains(".mpd", true) -> StreamType.DASH
        url.contains(".mp4", true) || url.contains(".mkv", true) -> StreamType.MP4
        url.startsWith("rtmp://", true) || url.startsWith("rtmps://", true) -> StreamType.RTMP
        else -> StreamType.HLS // assume HLS for bare HTTP streams
    }
}
