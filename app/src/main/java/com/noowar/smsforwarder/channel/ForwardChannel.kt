package com.noowar.smsforwarder.channel

data class ForwardMessage(val destination: String, val text: String)

interface ForwardChannel {
    suspend fun send(message: ForwardMessage): Result<Unit>
}
