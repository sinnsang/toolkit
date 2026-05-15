package com.noowar.smsforwarder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardRuleDao {
    @Query("SELECT * FROM forward_rules ORDER BY id ASC")
    fun getAll(): Flow<List<ForwardRule>>

    @Query("SELECT * FROM forward_rules WHERE isEnabled = 1")
    suspend fun getEnabledRules(): List<ForwardRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ForwardRule): Long

    @Update
    suspend fun update(rule: ForwardRule)

    @Delete
    suspend fun delete(rule: ForwardRule)
}
