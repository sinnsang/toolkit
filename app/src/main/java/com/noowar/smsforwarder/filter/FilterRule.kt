package com.noowar.smsforwarder.filter

data class FilterRule(
    val id: Long = 0L,
    val senderPattern: String = "",
    val bodyPattern: String = "",
    val matchMode: MatchMode = MatchMode.OR,
    val isEnabled: Boolean = true
) {
    enum class MatchMode { AND, OR }

    fun matches(sender: String, body: String): Boolean {
        val activeSender = senderPattern.isNotEmpty()
        val activeBody = bodyPattern.isNotEmpty()
        if (!activeSender && !activeBody) return false

        val senderMatches = activeSender && sender.contains(senderPattern, ignoreCase = true)
        val bodyMatches = activeBody && body.contains(bodyPattern, ignoreCase = true)

        return when (matchMode) {
            MatchMode.AND -> (!activeSender || senderMatches) && (!activeBody || bodyMatches)
            MatchMode.OR -> senderMatches || bodyMatches
        }
    }
}
