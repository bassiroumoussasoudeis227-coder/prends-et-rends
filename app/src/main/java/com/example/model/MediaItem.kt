package com.example.model

import java.util.UUID

enum class MediaType {
    PHOTO,
    VIDEO
}

data class MediaItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: String = "",
    val type: MediaType = MediaType.PHOTO,
    val thumbnailUri: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uri" to uri,
        "type" to type.name,
        "thumbnailUri" to thumbnailUri
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): MediaItem {
            return MediaItem(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                uri = map["uri"] as? String ?: "",
                type = try {
                    MediaType.valueOf(map["type"] as? String ?: MediaType.PHOTO.name)
                } catch (_: Exception) {
                    MediaType.PHOTO
                },
                thumbnailUri = map["thumbnailUri"] as? String
            )
        }
    }
}
