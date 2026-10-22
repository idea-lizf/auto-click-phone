package com.autoclick.app.locator

import android.graphics.Rect

class ElementLocator {

    fun findBySelector(rootNodes: List<NodeInfo>, selector: Selector): List<NodeInfo> {
        if (selector.point != null) {
            val x = selector.point.x
            val y = selector.point.y
            return rootNodes.mapNotNull { root -> findDeepestClickableAt(root, x, y) }
                .ifEmpty { rootNodes.mapNotNull { findDeepestAt(it, x, y) } }
        }

        val results = mutableListOf<NodeInfo>()
        for (root in rootNodes) {
            findMatching(root, selector, results)
        }
        return results
    }

    fun findFirst(rootNodes: List<NodeInfo>, selector: Selector): NodeInfo? {
        val matches = findBySelector(rootNodes, selector)
        return matches.firstOrNull()
    }

    fun isVisible(node: NodeInfo, screenWidth: Int, screenHeight: Int): Boolean {
        val b = node.bounds
        if (b.width() <= 0 || b.height() <= 0) return false
        if (b.left >= screenWidth || b.right <= 0) return false
        if (b.top >= screenHeight || b.bottom <= 0) return false
        return true
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

    private fun findDeepestClickableAt(root: NodeInfo, x: Int, y: Int): NodeInfo? {
        var best: NodeInfo? = null
        var bestArea = Int.MAX_VALUE

        fun walk(node: NodeInfo) {
            if (node.isClickable && node.containsPoint(x, y)) {
                val area = node.bounds.width() * node.bounds.height()
                if (area < bestArea) {
                    bestArea = area
                    best = node
                }
            }
            for (child in node.children) walk(child)
        }
        walk(root)
        return best
    }

    private fun findDeepestAt(root: NodeInfo, x: Int, y: Int): NodeInfo? {
        var best: NodeInfo? = null
        var bestArea = Int.MAX_VALUE

        fun walk(node: NodeInfo) {
            if (node.containsPoint(x, y)) {
                val area = node.bounds.width() * node.bounds.height()
                if (area < bestArea) {
                    bestArea = area
                    best = node
                }
            }
            for (child in node.children) walk(child)
        }
        walk(root)
        return best
    }

    companion object {
        fun boundsCenter(bounds: Rect): Pair<Int, Int> = bounds.centerX() to bounds.centerY()
    }
}
