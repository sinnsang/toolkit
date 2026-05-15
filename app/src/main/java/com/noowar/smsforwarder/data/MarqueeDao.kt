package com.noowar.smsforwarder.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarqueeDao {
    @Query("SELECT * FROM marquee_items ORDER BY id DESC")
    fun getAll(): Flow<List<MarqueeItem>>

    @Insert
    suspend fun insert(item: MarqueeItem)

    @Update
    suspend fun update(item: MarqueeItem)

    @Delete
    suspend fun delete(item: MarqueeItem)
}
