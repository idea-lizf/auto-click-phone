package com.autoclick.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.autoclick.app.locator.NodeInfo
import com.autoclick.app.locator.Selector
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object ServiceConnection {
    @Volatile
    private var instance: AutoClickAccessibilityService? = null

    fun onConnect(service: AutoClickAccessibilityService) {
        instance = service
    }

    fun onDisconnect() {
        instance = null
    }

    fun get(): AutoClickAccessibilityService? = instance

    fun isConnected(): Boolean = instance != null
}

class AccessibilityGatewayImpl : AccessibilityGateway {

    private fun service(): AutoClickAccessibilityService =
        ServiceConnection.get()
            ?: throw IllegalStateException("AccessibilityService not connected")

    override val isConnected: Boolean
        get() = ServiceConnection.isConnected()

    override fun rootNodes(): List<NodeInfo> {
        val service = service()
        val roots = mutableListOf<AccessibilityNodeInfo>()
        service.rootInActiveWindow?.let { roots.add(it) }
        return roots.map { it.toNodeInfo() }
    }

    override fun findNodes(selector: Selector): List<NodeInfo> {
        val roots = rootNodes()
        val results = mutableListOf<NodeInfo>()
        for (root in roots) {
            findMatching(root, selector, results)
        }
        return results
    }

    override suspend fun dispatchTap(x: Float, y: Float, durationMs: Long): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs.coerceAtLeast(1L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture)
    }

    override suspend fun dispatchSwipe(
        fromX: Float, fromY: Float,
        toX: Float, toY: Float,
        durationMs: Long
    ): Boolean {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs.coerceAtLeast(1L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture)
    }

    override fun pressBack(): Boolean {
        return service().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    override fun pressHome(): Boolean {
        return service().performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    override fun inputText(text: String): Boolean {
        val service = service()
        val focused = service.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: service.rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return false

        val args = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val result = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        focused.recycle()
        return result
    }

    override fun launchApp(packageName: String): Boolean {
        val service = service()
        val intent = service.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        service.startActivity(intent)
        return true
    }

    private suspend fun dispatchGesture(gesture: GestureDescription): Boolean =
        suspendCancellableCoroutine { cont ->
            val service = service()
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (cont.isActive) cont.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    if (cont.isActive) cont.resume(false)
                }
            }
            val dispatched = service.dispatchGesture(gesture, callback, null)
            if (!dispatched && cont.isActive) {
                cont.resume(false)
            }
        }

    private fun findMatching(
        node: NodeInfo,
        selector: Selector,
        results: MutableList<NodeInfo>
    ) {
        if (selector.matches(node)) {
            results.add(node)
        }
        for (child in node.children) {
            findMatching(child, selector, results)
        }
    }
}

private fun AccessibilityNodeInfo.toNodeInfo(): NodeInfo {
    val bounds = Rect().also { getBoundsInScreen(it) }
    val children = (0 until childCount).mapNotNull { i ->
        getChild(i)?.toNodeInfo()
    }
    return NodeInfo(
        viewIdResourceName = viewIdResourceName,
        text = text,
        contentDescription = contentDescription,
        className = className?.toString(),
        bounds = bounds,
        isClickable = isClickable,
        isScrollable = isScrollable,
        children = children
    )
}
