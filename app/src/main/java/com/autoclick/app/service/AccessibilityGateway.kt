package com.autoclick.app.service

import com.autoclick.app.locator.NodeInfo
import com.autoclick.app.locator.Selector

interface AccessibilityGateway {
    val isConnected: Boolean

    fun rootNodes(): List<NodeInfo>

    fun findNodes(selector: Selector): List<NodeInfo>

    suspend fun dispatchTap(x: Float, y: Float, durationMs: Long): Boolean

    suspend fun dispatchSwipe(
        fromX: Float, fromY: Float,
        toX: Float, toY: Float,
        durationMs: Long
    ): Boolean

    fun pressBack(): Boolean

    fun pressHome(): Boolean

    fun inputText(text: String): Boolean

    fun launchApp(packageName: String): Boolean
}
