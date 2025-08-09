package com.example.focuslock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val addedAt: Long,
    val iconBlob: ByteArray? = null // optional cache to show icon offline
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockedApp

        if (packageName != other.packageName) return false
        if (appLabel != other.appLabel) return false
        if (addedAt != other.addedAt) return false
        if (iconBlob != null) {
            if (other.iconBlob == null) return false
            if (!iconBlob.contentEquals(other.iconBlob)) return false
        } else if (other.iconBlob != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + appLabel.hashCode()
        result = 31 * result + addedAt.hashCode()
        result = 31 * result + (iconBlob?.contentHashCode() ?: 0)
        return result
    }
} 