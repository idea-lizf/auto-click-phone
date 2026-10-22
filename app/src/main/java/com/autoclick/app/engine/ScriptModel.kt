package com.autoclick.app.engine

import com.autoclick.app.locator.Selector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Script(
    val version: Int = 1,
    val name: String,
    val targetPackage: String? = null,
    val repeat: Int = 1,
    val steps: List<Step>
)

@Serializable
data class Step(
    val action: String,
    val target: Selector? = null,
    val point: Point? = null,
    val from: Point? = null,
    val to: Point? = null,
    val direction: String? = null,
    val distance: Int? = null,
    val duration: Long? = null,
    val packageName: String? = null,
    val text: String? = null,
    val seconds: Double? = null,
    val timeout: Int? = null,
    val maxSwipes: Int? = null,
    val retry: Int = 0
)

@Serializable
data class Point(
    val x: Int,
    val y: Int
)

enum class StepAction(val value: String) {
    CLICK("click"),
    LONG_PRESS("longPress"),
    SWIPE("swipe"),
    BACK("back"),
    HOME("home"),
    LAUNCH_APP("launchApp"),
    INPUT_TEXT("inputText"),
    WAIT_FOR("waitFor"),
    DELAY("delay"),
    SCROLL_TO("scrollTo");

    companion object {
        fun fromValue(value: String): StepAction? = entries.find { it.value == value }
    }
}
