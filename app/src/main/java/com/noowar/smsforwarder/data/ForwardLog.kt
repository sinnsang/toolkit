package com.noowar.smsforwarder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forward_logs")
data class ForwardLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val fromNumber: String,
    val toNumber: String,
    val body: String,
    val ruleId: Long,
    val success: Boolean,
    val errorMessage: String? = null
)
