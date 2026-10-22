package com.autoclick.app.locator

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectorTest {

    private fun makeNode(
        id: String? = null,
        text: CharSequence? = null,
        desc: CharSequence? = null,
        className: String? = null,
        clickable: Boolean = false,
        bounds: Rect = Rect(0, 0, 100, 100)
    ) = NodeInfo(id, text, desc, className, bounds, clickable, false, emptyList())

    @Test
    fun matches_byId() {
        val selector = Selector(id = "com.example:id/btn")
        val node = makeNode(id = "com.example:id/btn")
        assertTrue(selector.matches(node))
    }

    @Test
    fun matches_byId_negative() {
        val selector = Selector(id = "com.example:id/btn")
        val node = makeNode(id = "com.example:id/other")
        assertFalse(selector.matches(node))
    }

    @Test
    fun matches_byText_exact() {
        val selector = Selector(text = "签到")
        val node = makeNode(text = "签到")
        assertTrue(selector.matches(node))
    }

    @Test
    fun matches_byText_notContains() {
        val selector = Selector(text = "签到")
        val node = makeNode(text = "每日签到")
        assertFalse(selector.matches(node))
    }

    @Test
    fun matches_byTextContains() {
        val selector = Selector(textContains = "签到")
        val node = makeNode(text = "每日签到")
        assertTrue(selector.matches(node))
    }

    @Test
    fun matches_byDesc() {
        val selector = Selector(desc = "按钮")
        val node = makeNode(desc = "按钮")
        assertTrue(selector.matches(node))
    }

    @Test
    fun matches_byText_withClassName() {
        val selector = Selector(text = "签到", className = "android.widget.Button")
        val node = makeNode(text = "签到", className = "android.widget.Button")
        assertTrue(selector.matches(node))
    }

    @Test
    fun matches_byText_wrongClassName() {
        val selector = Selector(text = "签到", className = "android.widget.Button")
        val node = makeNode(text = "签到", className = "android.widget.TextView")
        assertFalse(selector.matches(node))
    }

    @Test
    fun priority_idFirst() {
        val selector = Selector(id = "x", text = "y")
        assertEquals(SelectorPriority.ID, selector.priority)
    }

    @Test
    fun priority_textBeforeTextContains() {
        val selector = Selector(text = "y", textContains = "z")
        assertEquals(SelectorPriority.TEXT, selector.priority)
    }

    @Test
    fun priority_pointFallback() {
        val selector = Selector(point = Selector.PointSelector(100, 200))
        assertEquals(SelectorPriority.POINT, selector.priority)
    }
}
