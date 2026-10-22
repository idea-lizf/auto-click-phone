package com.autoclick.app.locator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Selector(
    val id: String? = null,
    val text: String? = null,
    val textContains: String? = null,
    val desc: String? = null,
    val className: String? = null,
    val point: PointSelector? = null,
    val anchor: String = "center"
) {
    @Serializable
    data class PointSelector(
        val x: Int,
        val y: Int
    )

    val priority: SelectorPriority
        get() = when {
            id != null -> SelectorPriority.ID
            text != null -> SelectorPriority.TEXT
            textContains != null -> SelectorPriority.TEXT_CONTAINS
            desc != null -> SelectorPriority.DESC
            point != null -> SelectorPriority.POINT
            else -> SelectorPriority.NONE
        }

    fun matches(node: NodeInfo): Boolean {
        var matched = false
        if (id != null && node.viewIdResourceName == id) matched = true
        if (text != null && node.text?.toString() == text) matched = true
        if (textContains != null && node.text?.toString()?.contains(textContains) == true) matched = true
        if (desc != null && node.contentDescription?.toString() == desc) matched = true
        if (point != null && id == null && text == null && textContains == null && desc == null) matched = true
        if (!matched) return false
        if (className != null && node.className != className) return false
        return true
    }

    override fun toString(): String {
        val parts = mutableListOf<String>()
        id?.let { parts.add("id=$it") }
        text?.let { parts.add("text=$it") }
        textContains?.let { parts.add("textContains=$it") }
        desc?.let { parts.add("desc=$it") }
        className?.let { parts.add("className=$it") }
        point?.let { parts.add("point=${it.x},${it.y}") }
        return parts.joinToString(", ")
    }
}

enum class SelectorPriority {
    ID, TEXT, TEXT_CONTAINS, DESC, POINT, NONE
}
