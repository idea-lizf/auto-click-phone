package com.autoclick.app.engine

import com.autoclick.app.locator.Selector
import kotlinx.serialization.json.Json

object ScriptParser {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun parse(jsonString: String): Script {
        return json.decodeFromString(Script.serializer(), jsonString)
    }

    fun toJson(script: Script): String {
        return json.encodeToString(Script.serializer(), script)
    }

    fun validate(script: Script): List<String> {
        val errors = mutableListOf<String>()
        if (script.name.isBlank()) errors.add("脚本名称不能为空")
        if (script.steps.isEmpty()) errors.add("步骤列表不能为空")
        script.steps.forEachIndexed { i, step ->
            val action = StepAction.fromValue(step.action)
            if (action == null) {
                errors.add("步骤 ${i + 1}: 未知 action '${step.action}'")
            } else {
                val stepErr = validateStep(step, action)
                if (stepErr != null) errors.add("步骤 ${i + 1}: $stepErr")
            }
        }
        return errors
    }

    private fun validateStep(step: Step, action: StepAction): String? = when (action) {
        StepAction.CLICK, StepAction.LONG_PRESS ->
            if (step.target == null && step.point == null) "click/longPress 需要 target 或 point"
            else null

        StepAction.SWIPE ->
            if (step.from != null && step.to != null) null
            else if (step.direction != null) null
            else "swipe 需要 from+to 或 direction"

        StepAction.LAUNCH_APP ->
            if (step.packageName.isNullOrBlank()) "launchApp 需要 packageName"
            else null

        StepAction.INPUT_TEXT ->
            if (step.text == null) "inputText 需要 text"
            else null

        StepAction.WAIT_FOR, StepAction.SCROLL_TO ->
            if (step.target == null) "${action.value} 需要 target"
            else null

        StepAction.DELAY ->
            if (step.seconds == null) "delay 需要 seconds"
            else null

        StepAction.BACK, StepAction.HOME -> null
    }
}
