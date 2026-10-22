package com.autoclick.app.locator

import android.graphics.Rect

data class NodeInfo(
    val viewIdResourceName: String?,
    val text: CharSequence?,
    val contentDescription: CharSequence?,
    val className: String?,
    val bounds: Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val children: List<NodeInfo>
) {
    val centerX: Int get() = bounds.centerX()
    val centerY: Int get() = bounds.centerY()

    fun containsPoint(x: Int, y: Int): Boolean = bounds.contains(x, y)

    fun findClickableAncestorOrSelf(): NodeInfo? {
        var node: NodeInfo? = this
        while (node != null) {
            if (node.isClickable) return node
            node = node.children.firstOrNull { it.containsPoint(centerX, centerY) }
        }
        return null
    }
}
