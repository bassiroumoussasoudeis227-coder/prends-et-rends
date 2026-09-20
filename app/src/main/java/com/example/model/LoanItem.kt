package com.example.model

import java.util.UUID

enum class LoanType(val label: String) {
    LENT("Je prête"),
    BORROWED("J'emprunte")
}

enum class LoanCategory(val label: String) {
    TOOLS("Outils"),
    BOOKS("Livres"),
    HIGH_TECH("High-Tech"),
    MONEY("Argent"),
    GAMES("Jeux"),
    CLOTHES("Vêtements"),
    OTHER("Autre")
}

enum class LoanStatus(val label: String) {
    ACTIVE("En cours"),
    RETURNED("Rendu")
}

data class LoanItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val personName: String = "",
    val type: LoanType = LoanType.LENT,
    val category: LoanCategory = LoanCategory.OTHER,
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val notes: String = "",
    val status: LoanStatus = LoanStatus.ACTIVE,
    val returnedDate: Long? = null,
    val contactPhone: String = "",
    // Satellite Location Tracking
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    // Voice Note Audio
    val audioPath: String? = null,
    val audioDurationSeconds: Int = 0,
    // Multi-Media Attachments (photos & videos)
    val mediaAttachments: List<MediaItem> = emptyList()
) {
    val isOverdue: Boolean
        get() = status == LoanStatus.ACTIVE && dueDate != null && dueDate < System.currentTimeMillis()

    val daysRemaining: Long?
        get() {
            val due = dueDate ?: return null
            val diff = due - System.currentTimeMillis()
            return diff / (1000 * 60 * 60 * 24)
        }

    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "personName" to personName,
        "type" to type.name,
        "category" to category.name,
        "startDate" to startDate,
        "dueDate" to dueDate,
        "notes" to notes,
        "status" to status.name,
        "returnedDate" to returnedDate,
        "contactPhone" to contactPhone,
        "latitude" to latitude,
        "longitude" to longitude,
        "address" to address,
        "audioPath" to audioPath,
        "audioDurationSeconds" to audioDurationSeconds,
        "mediaAttachments" to mediaAttachments.map { it.toFirestoreMap() }
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): LoanItem {
            val rawMedia = map["mediaAttachments"] as? List<Map<String, Any?>> ?: emptyList()
            val attachments = rawMedia.map { MediaItem.fromFirestoreMap(it) }

            return LoanItem(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                title = map["title"] as? String ?: "",
                personName = map["personName"] as? String ?: "",
                type = try {
                    LoanType.valueOf(map["type"] as? String ?: LoanType.LENT.name)
                } catch (_: Exception) {
                    LoanType.LENT
                },
                category = try {
                    LoanCategory.valueOf(map["category"] as? String ?: LoanCategory.OTHER.name)
                } catch (_: Exception) {
                    LoanCategory.OTHER
                },
                startDate = (map["startDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                dueDate = (map["dueDate"] as? Number)?.toLong(),
                notes = map["notes"] as? String ?: "",
                status = try {
                    LoanStatus.valueOf(map["status"] as? String ?: LoanStatus.ACTIVE.name)
                } catch (_: Exception) {
                    LoanStatus.ACTIVE
                },
                returnedDate = (map["returnedDate"] as? Number)?.toLong(),
                contactPhone = map["contactPhone"] as? String ?: "",
                latitude = (map["latitude"] as? Number)?.toDouble(),
                longitude = (map["longitude"] as? Number)?.toDouble(),
                address = map["address"] as? String,
                audioPath = map["audioPath"] as? String,
                audioDurationSeconds = (map["audioDurationSeconds"] as? Number)?.toInt() ?: 0,
                mediaAttachments = attachments
            )
        }
    }
}
