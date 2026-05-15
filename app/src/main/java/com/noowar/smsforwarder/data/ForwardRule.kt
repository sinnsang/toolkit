package com.noowar.smsforwarder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forward_rules")
data class ForwardRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isEnabled: Boolean = true,
    val senderFilter: String = "",
    val bodyFilter: String = "",
    val matchMode: String = "OR",
    val destination: String = "",
    val messageFormat: String = "ORIGINAL",
    val formatString: String = "",
    val channelType: String = "SMS"
) {
    fun matches(sender: String, body: String): Boolean {
        val activeSender = senderFilter.isNotEmpty()
        val activeBody = bodyFilter.isNotEmpty()
        if (!activeSender && !activeBody) return true

        val senderMatches = activeSender && sender.contains(senderFilter, ignoreCase = true)
        val bodyMatches = activeBody && body.contains(bodyFilter, ignoreCase = true)

        return when (matchMode) {
            "AND" -> (!activeSender || senderMatches) && (!activeBody || bodyMatches)
            else -> senderMatches || bodyMatches
        }
    }

    fun formatMessage(originalSender: String, originalBody: String): String {
        return when (messageFormat) {
            "PREPEND" -> if (formatString.isNotEmpty()) "$formatString\n$originalBody" else "[$originalSender]\n$originalBody"
            "APPEND" -> if (formatString.isNotEmpty()) "$originalBody\n$formatString" else "$originalBody\n[$originalSender]"
            "REPLACE" -> formatString
            else -> originalBody
        }
    }
}
