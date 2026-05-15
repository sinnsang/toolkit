package com.noowar.smsforwarder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardLogDao {
    @Query("SELECT * FROM forward_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecent(): Flow<List<ForwardLog>>

    @Insert
    suspend fun insert(log: ForwardLog)

    @Query("DELETE FROM forward_logs WHERE timestamp < :before")
    suspend fun deleteBefore(before: Long)
}
