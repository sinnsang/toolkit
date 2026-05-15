package com.noowar.smsforwarder.channel

object ForwardDedup {
    private val recent = LinkedHashMap<String, Long>()
    private const val WINDOW_MS = 5000L

    @Synchronized
    fun claim(key: String): Boolean {
        val now = System.currentTimeMillis()
        recent.entries.removeIf { now - it.value > WINDOW_MS }
        return recent.putIfAbsent(key, now) == null
    }
}
