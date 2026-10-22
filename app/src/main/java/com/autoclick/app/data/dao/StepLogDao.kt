package com.autoclick.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autoclick.app.data.entity.StepLogEntity

@Dao
interface StepLogDao {
    @Query("SELECT * FROM step_logs WHERE runId = :runId ORDER BY stepIndex ASC, timestamp ASC")
    fun observeByRun(runId: Long): kotlinx.coroutines.flow.Flow<List<StepLogEntity>>

    @Query("SELECT * FROM step_logs WHERE runId = :runId ORDER BY stepIndex ASC, timestamp ASC")
    suspend fun getByRun(runId: Long): List<StepLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StepLogEntity): Long

    @Query("DELETE FROM step_logs WHERE runId = :runId")
    suspend fun deleteByRun(runId: Long)
}
