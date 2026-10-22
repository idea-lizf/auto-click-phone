package com.autoclick.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_logs")
data class RunLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scriptId: Long,
    val scriptName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val status: RunStatus = RunStatus.RUNNING,
    val currentStepIndex: Int = 0,
    val errorMessage: String? = null
)

enum class RunStatus {
    RUNNING, SUCCESS, FAILED, CANCELED
}
