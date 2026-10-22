package com.autoclick.app.engine

import com.autoclick.app.locator.ElementLocator
import com.autoclick.app.locator.NodeInfo
import com.autoclick.app.service.AccessibilityGateway
import kotlinx.coroutines.delay

class StepExecutor(
    private val gateway: AccessibilityGateway,
    private val locator: ElementLocator = ElementLocator()
) {
    suspend fun execute(step: Step): StepResult {
        val action = StepAction.fromValue(step.action)
            ?: return StepResult.Failed("未知 action: ${step.action}")

        return when (action) {
            StepAction.CLICK -> executeClick(step)
            StepAction.LONG_PRESS -> executeLongPress(step)
            StepAction.SWIPE -> executeSwipe(step)
            StepAction.BACK -> executeBack()
            StepAction.HOME -> executeHome()
            StepAction.LAUNCH_APP -> executeLaunchApp(step)
            StepAction.INPUT_TEXT -> executeInputText(step)
            StepAction.WAIT_FOR -> executeWaitFor(step)
            StepAction.DELAY -> executeDelay(step)
            StepAction.SCROLL_TO -> executeScrollTo(step)
        }
    }

    private suspend fun executeClick(step: Step): StepResult {
        val (x, y) = resolveTargetPoint(step)
            ?: return StepResult.Failed("无法定位点击目标")
        val ok = gateway.dispatchTap(x.toFloat(), y.toFloat(), step.duration ?: DEFAULT_TAP_MS)
        return if (ok) StepResult.Success else StepResult.Failed("点击派发失败")
    }

    private suspend fun executeLongPress(step: Step): StepResult {
        val (x, y) = resolveTargetPoint(step)
            ?: return StepResult.Failed("无法定位长按目标")
        val duration = step.duration ?: DEFAULT_LONG_PRESS_MS
        val ok = gateway.dispatchTap(x.toFloat(), y.toFloat(), duration)
        return if (ok) StepResult.Success else StepResult.Failed("长按派发失败")
    }

    private suspend fun executeSwipe(step: Step): StepResult {
        val (fromX, fromY, toX, toY) = resolveSwipePoints(step)
            ?: return StepResult.Failed("swipe 需要 from+to 或 direction+distance")
        val duration = step.duration ?: DEFAULT_SWIPE_MS
        val ok = gateway.dispatchSwipe(fromX, fromY, toX, toY, duration)
        return if (ok) StepResult.Success else StepResult.Failed("滑动派发失败")
    }

    private fun executeBack(): StepResult {
        return if (gateway.pressBack()) StepResult.Success
        else StepResult.Failed("返回键失败")
    }

    private fun executeHome(): StepResult {
        return if (gateway.pressHome()) StepResult.Success
        else StepResult.Failed("Home 键失败")
    }

    private fun executeLaunchApp(step: Step): StepResult {
        val pkg = step.packageName
        if (pkg.isNullOrBlank()) return StepResult.Failed("launchApp 需要 packageName")
        return if (gateway.launchApp(pkg)) StepResult.Success
        else StepResult.Failed("启动 App 失败: $pkg")
    }

    private fun executeInputText(step: Step): StepResult {
        val text = step.text ?: return StepResult.Failed("inputText 需要 text")
        return if (gateway.inputText(text)) StepResult.Success
        else StepResult.Failed("输入文本失败")
    }

    private suspend fun executeWaitFor(step: Step): StepResult {
        val target = step.target
            ?: return StepResult.Failed("waitFor 需要 target")
        val timeoutMs = (step.timeout ?: DEFAULT_WAIT_TIMEOUT_SEC) * 1000L
        val intervalMs = POLL_INTERVAL_MS

        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val nodes = gateway.findNodes(target)
            if (nodes.isNotEmpty()) return StepResult.Success
            delay(intervalMs)
        }
        return StepResult.Failed("等待元素超时: $target")
    }

    private suspend fun executeDelay(step: Step): StepResult {
        val seconds = step.seconds
            ?: return StepResult.Failed("delay 需要 seconds")
        delay((seconds * 1000).toLong())
        return StepResult.Success
    }

    private suspend fun executeScrollTo(step: Step): StepResult {
        val target = step.target
            ?: return StepResult.Failed("scrollTo 需要 target")
        val maxSwipes = step.maxSwipes ?: DEFAULT_MAX_SWIPES

        for (i in 1..maxSwipes) {
            val nodes = gateway.findNodes(target)
            if (nodes.isNotEmpty()) return StepResult.Success
            val root = gateway.rootNodes().firstOrNull()
                ?: return StepResult.Failed("无法获取屏幕尺寸")
            val cx = root.bounds.centerX().toFloat()
            val height = root.bounds.height()
            val ok = gateway.dispatchSwipe(
                cx, (height * 0.7).toFloat(),
                cx, (height * 0.3).toFloat(),
                DEFAULT_SWIPE_MS
            )
            if (!ok) return StepResult.Failed("滑动失败 (第 $i 次)")
            delay(POST_SWIPE_DELAY_MS)
        }
        return StepResult.Failed("滑动 $maxSwipes 次后仍未找到元素")
    }

    private suspend fun resolveTargetPoint(step: Step): Pair<Int, Int>? {
        if (step.point != null) return step.point.x to step.point.y
        val target = step.target ?: return null
        val nodes = gateway.findNodes(target)
        return nodes.firstOrNull()?.let { it.centerX to it.centerY }
    }

    private fun resolveSwipePoints(step: Step): FloatArray? {
        if (step.from != null && step.to != null) {
            return floatArrayOf(
                step.from.x.toFloat(), step.from.y.toFloat(),
                step.to.x.toFloat(), step.to.y.toFloat()
            )
        }
        val direction = step.direction ?: return null
        val distance = step.distance ?: DEFAULT_SWIPE_DISTANCE
        val screen = gateway.rootNodes().firstOrNull() ?: return null
        val cx = screen.bounds.centerX().toFloat()
        val cy = screen.bounds.centerY().toFloat()
        return when (direction.lowercase()) {
            "up" -> floatArrayOf(cx, cy + distance / 2f, cx, cy - distance / 2f)
            "down" -> floatArrayOf(cx, cy - distance / 2f, cx, cy + distance / 2f)
            "left" -> floatArrayOf(cx + distance / 2f, cy, cx - distance / 2f, cy)
            "right" -> floatArrayOf(cx - distance / 2f, cy, cx + distance / 2f, cy)
            else -> null
        }
    }

    companion object {
        private const val DEFAULT_TAP_MS = 50L
        private const val DEFAULT_LONG_PRESS_MS = 1500L
        private const val DEFAULT_SWIPE_MS = 400L
        private const val DEFAULT_WAIT_TIMEOUT_SEC = 10
        private const val POLL_INTERVAL_MS = 500L
        private const val DEFAULT_MAX_SWIPES = 10
        private const val DEFAULT_SWIPE_DISTANCE = 500
        private const val POST_SWIPE_DELAY_MS = 300L

        private val NodeInfo.centerX get() = bounds.centerX()
        private val NodeInfo.centerY get() = bounds.centerY()
    }
}
