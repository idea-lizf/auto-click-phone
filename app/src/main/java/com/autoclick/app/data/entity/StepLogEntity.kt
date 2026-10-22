package com.autoclick.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_logs")
data class StepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val runId: Long,
    val stepIndex: Int,
    val action: String,
    val status: StepStatus,
    val detail: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StepStatus {
    STARTED, SUCCESS, FAILED
}
