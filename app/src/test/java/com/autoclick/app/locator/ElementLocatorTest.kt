package com.autoclick.app.locator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ElementLocatorTest {

    private val locator = ElementLocator()

    private fun makeNode(
        text: String? = null,
        id: String? = null,
        children: List<NodeInfo> = emptyList()
    ) = NodeInfo(id, text, null, null, android.graphics.Rect(), false, false, children)

    @Test
    fun findFirst_byText_inRoot() {
        val root = makeNode(text = "root", children = listOf(
            makeNode(text = "A"),
            makeNode(text = "B")
        ))
        val result = locator.findFirst(listOf(root), Selector(text = "B"))
        assertNotNull(result)
        assertEquals("B", result?.text?.toString())
    }

    @Test
    fun findFirst_notFound_returnsNull() {
        val root = makeNode(text = "root")
        val result = locator.findFirst(listOf(root), Selector(text = "X"))
        assertNull(result)
    }

    @Test
    fun findFirst_deepNesting() {
        val deep = makeNode(text = "deep")
        val mid = makeNode(text = "mid", children = listOf(deep))
        val root = makeNode(text = "root", children = listOf(mid))

        val result = locator.findFirst(listOf(root), Selector(text = "deep"))
        assertNotNull(result)
        assertEquals("deep", result?.text?.toString())
    }
}
