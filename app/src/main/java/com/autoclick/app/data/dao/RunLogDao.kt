package com.autoclick.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autoclick.app.data.entity.RunLogEntity
import com.autoclick.app.data.entity.RunStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RunLogDao {
    @Query("SELECT * FROM run_logs ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<RunLogEntity>>

    @Query("SELECT * FROM run_logs WHERE id = :id")
    suspend fun getById(id: Long): RunLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RunLogEntity): Long

    @Query("UPDATE run_logs SET finishedAt = :finishedAt, status = :status, currentStepIndex = :stepIndex, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateResult(
        id: Long,
        finishedAt: Long,
        status: RunStatus,
        stepIndex: Int,
        errorMessage: String? = null
    )

    @Query("DELETE FROM run_logs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM run_logs WHERE startedAt < (SELECT MIN(startedAt) FROM (SELECT startedAt FROM run_logs ORDER BY startedAt DESC LIMIT :keep))")
    suspend fun trim(keep: Int)

    @Query("DELETE FROM run_logs")
    suspend fun deleteAll()
}
