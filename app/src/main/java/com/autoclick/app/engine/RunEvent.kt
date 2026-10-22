package com.autoclick.app.engine

sealed class RunEvent {
    data class RunStarted(
        val runId: Long,
        val scriptName: String,
        val totalSteps: Int,
        val repeat: Int
    ) : RunEvent()

    data class StepStarted(
        val repeatIndex: Int,
        val stepIndex: Int,
        val action: String,
        val detail: String
    ) : RunEvent()

    data class StepSuccess(
        val repeatIndex: Int,
        val stepIndex: Int,
        val action: String
    ) : RunEvent()

    data class StepFailed(
        val repeatIndex: Int,
        val stepIndex: Int,
        val action: String,
        val message: String,
        val willRetry: Boolean
    ) : RunEvent()

    data class RunCompleted(
        val runId: Long,
        val success: Boolean,
        val message: String? = null
    ) : RunEvent()
}
