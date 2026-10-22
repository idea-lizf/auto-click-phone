package com.autoclick.app.engine

import com.autoclick.app.data.RunLogRepository
import com.autoclick.app.data.entity.RunStatus
import com.autoclick.app.data.entity.StepLogEntity
import com.autoclick.app.data.entity.StepStatus
import com.autoclick.app.service.AccessibilityGateway
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

class ScriptEngine(
    private val gateway: AccessibilityGateway,
    private val runLogRepository: RunLogRepository? = null,
    private val stepExecutor: StepExecutor = StepExecutor(gateway),
    private val stepDelayMs: Long = DEFAULT_STEP_DELAY_MS,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS
) {

    fun run(script: Script, scriptId: Long = 0): Flow<RunEvent> = flow {
        require(gateway.isConnected) {
            "无障碍服务未连接，请先开启无障碍权限"
        }

        val runId = runLogRepository?.startRun(scriptId, script.name) ?: 0L
        emit(RunEvent.RunStarted(runId, script.name, script.steps.size, script.repeat))

        var success = true
        var lastError: String? = null
        var lastStepIndex = 0

        try {
            for (repeatIndex in 0 until script.repeat) {
                if (!success) break
                for ((stepIndex, step) in script.steps.withIndex()) {
                    lastStepIndex = stepIndex
                    val detail = step.describe()
                    emit(RunEvent.StepStarted(repeatIndex, stepIndex, step.action, detail))
                    runLogRepository?.logStep(
                        StepLogEntity(
                            runId = runId,
                            stepIndex = stepIndex,
                            action = step.action,
                            status = StepStatus.STARTED,
                            detail = detail
                        )
                    )

                    val result = executeWithRetry(step, repeatIndex, stepIndex, runId, stepIndex)

                    when (result) {
                        is StepResult.Success -> {
                            emit(RunEvent.StepSuccess(repeatIndex, stepIndex, step.action))
                            runLogRepository?.logStep(
                                StepLogEntity(
                                    runId = runId,
                                    stepIndex = stepIndex,
                                    action = step.action,
                                    status = StepStatus.SUCCESS,
                                    detail = detail
                                )
                            )
                        }
                        is StepResult.Failed -> {
                            emit(RunEvent.StepFailed(repeatIndex, stepIndex, step.action, result.message, false))
                            runLogRepository?.logStep(
                                StepLogEntity(
                                    runId = runId,
                                    stepIndex = stepIndex,
                                    action = step.action,
                                    status = StepStatus.FAILED,
                                    detail = "$detail | ${result.message}"
                                )
                            )
                            success = false
                            lastError = result.message
                            break
                        }
                    }
                    if (stepIndex < script.steps.size - 1) {
                        delay(stepDelayMs)
                    }
                }
            }

            emit(RunEvent.RunCompleted(runId, success, lastError))
            runLogRepository?.finishRun(
                runId,
                if (success) RunStatus.SUCCESS else RunStatus.FAILED,
                lastStepIndex,
                lastError
            )
        } catch (e: CancellationException) {
            emit(RunEvent.RunCompleted(runId, false, "已取消"))
            runLogRepository?.finishRun(runId, RunStatus.CANCELED, lastStepIndex, "已取消")
            throw e
        } catch (e: Exception) {
            emit(RunEvent.RunCompleted(runId, false, e.message ?: e::class.simpleName))
            runLogRepository?.finishRun(
                runId,
                RunStatus.FAILED,
                lastStepIndex,
                e.message ?: e::class.simpleName
            )
        }
    }

    private suspend fun FlowCollector<RunEvent>.executeWithRetry(
        step: Step,
        repeatIndex: Int,
        stepIndex: Int,
        runId: Long,
        realStepIndex: Int
    ): StepResult {
        var retriesLeft = step.retry
        var lastResult: StepResult = StepResult.Failed("未执行")

        while (true) {
            lastResult = if (step.timeout != null && step.action != StepAction.WAIT_FOR.value) {
                withTimeoutOrNull(step.timeout * 1000L) {
                    stepExecutor.execute(step)
                } ?: StepResult.Failed("步骤超时 (${step.timeout}s)")
            } else {
                stepExecutor.execute(step)
            }

            if (lastResult is StepResult.Success) return lastResult
            if (retriesLeft <= 0) return lastResult

            retriesLeft--
            emit(
                RunEvent.StepFailed(
                    repeatIndex, stepIndex, step.action,
                    (lastResult as StepResult.Failed).message,
                    willRetry = true
                )
            )
            runLogRepository?.logStep(
                StepLogEntity(
                    runId = runId,
                    stepIndex = realStepIndex,
                    action = step.action,
                    status = StepStatus.FAILED,
                    detail = "重试中: ${(lastResult as StepResult.Failed).message}"
                )
            )
            delay(retryDelayMs)
        }
    }

    private fun Step.describe(): String = buildString {
        append(action)
        target?.let { append(" [").append(it).append("]") }
        point?.let { append(" @(").append(it.x).append(",").append(it.y).append(")") }
        packageName?.let { append(" pkg=").append(it) }
        text?.let { append(" text=").append(it) }
        seconds?.let { append(" ").append(it).append("s") }
        timeout?.let { append(" timeout=").append(it).append("s") }
    }

    companion object {
        private const val DEFAULT_STEP_DELAY_MS = 500L
        private const val DEFAULT_RETRY_DELAY_MS = 1000L
    }
}
