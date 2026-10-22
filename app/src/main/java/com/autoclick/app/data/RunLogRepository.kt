package com.autoclick.app.data

import com.autoclick.app.data.dao.RunLogDao
import com.autoclick.app.data.dao.StepLogDao
import com.autoclick.app.data.entity.RunLogEntity
import com.autoclick.app.data.entity.RunStatus
import com.autoclick.app.data.entity.StepLogEntity
import kotlinx.coroutines.flow.Flow

class RunLogRepository(
    private val runLogDao: RunLogDao,
    private val stepLogDao: StepLogDao
) {

    fun observeRecent(limit: Int = 50): Flow<List<RunLogEntity>> = runLogDao.observeRecent(limit)

    fun observeStepLogs(runId: Long): Flow<List<StepLogEntity>> = stepLogDao.observeByRun(runId)

    suspend fun startRun(scriptId: Long, scriptName: String): Long {
        return runLogDao.insert(
            RunLogEntity(scriptId = scriptId, scriptName = scriptName)
        )
    }

    suspend fun finishRun(
        runId: Long,
        status: RunStatus,
        stepIndex: Int,
        errorMessage: String? = null
    ) {
        runLogDao.updateResult(
            id = runId,
            finishedAt = System.currentTimeMillis(),
            status = status,
            stepIndex = stepIndex,
            errorMessage = errorMessage
        )
        runLogDao.trim(keep = 50)
    }

    suspend fun logStep(entity: StepLogEntity) {
        stepLogDao.insert(entity)
    }

    suspend fun deleteRun(id: Long) {
        stepLogDao.deleteByRun(id)
        runLogDao.delete(id)
    }

    suspend fun deleteAll() {
        runLogDao.deleteAll()
    }
}
