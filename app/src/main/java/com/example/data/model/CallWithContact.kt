package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CallWithContact(
    @Embedded val callRecord: CallRecord,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "id"
    )
    val contact: Contact? = null
) {
    val displayName: String
        get() = contact?.name ?: callRecord.phoneNumber

    val displayPhoneNumber: String
        get() = callRecord.phoneNumber

    val formattedDuration: String
        get() = callRecord.formattedDuration
}
