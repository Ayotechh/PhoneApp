package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val contactId: Long? = null,
    val callType: CallType,
    val timestamp: Long,
    val durationSeconds: Int = 0
) {
    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return if (callType == CallType.MISSED) "Missed" else "0s"
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }
}
