package com.autoclick.app.engine

import com.autoclick.app.locator.Selector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ScriptParserTest {

    private val parser = ScriptParser

    @Test
    fun parse_fullScript() {
        val json = """
            {
              "version": 1,
              "name": "每日签到",
              "targetPackage": "com.example.app",
              "repeat": 3,
              "steps": [
                { "action": "launchApp", "packageName": "com.example.app" },
                { "action": "waitFor", "target": { "text": "我的" }, "timeout": 10 },
                { "action": "click", "target": { "text": "我的" } },
                { "action": "delay", "seconds": 1 },
                { "action": "back" }
              ]
            }
        """.trimIndent()

        val script = parser.parse(json)

        assertEquals(1, script.version)
        assertEquals("每日签到", script.name)
        assertEquals("com.example.app", script.targetPackage)
        assertEquals(3, script.repeat)
        assertEquals(5, script.steps.size)
        assertEquals("launchApp", script.steps[0].action)
        assertEquals("com.example.app", script.steps[0].packageName)
        assertEquals("waitFor", script.steps[1].action)
        assertEquals("我的", script.steps[1].target?.text)
        assertEquals(10, script.steps[1].timeout)
        assertEquals("click", script.steps[2].action)
        assertEquals("back", script.steps[4].action)
    }

    @Test
    fun parse_withUnknownFields_ignored() {
        val json = """
            {
              "name": "test",
              "repeat": 1,
              "steps": [
                { "action": "back", "unknownField": 42 }
              ]
            }
        """.trimIndent()

        val script = parser.parse(json)
        assertEquals("test", script.name)
        assertEquals(1, script.steps.size)
    }

    @Test
    fun parse_textContainsSelector() {
        val json = """
            {
              "name": "test",
              "repeat": 1,
              "steps": [
                { "action": "click", "target": { "textContains": "签到", "className": "android.widget.Button" } }
              ]
            }
        """.trimIndent()

        val script = parser.parse(json)
        val selector = script.steps[0].target!!
        assertEquals("签到", selector.textContains)
        assertEquals("android.widget.Button", selector.className)
        assertNull(selector.text)
        assertNull(selector.id)
    }

    @Test
    fun parse_pointSelector() {
        val json = """
            {
              "name": "test",
              "repeat": 1,
              "steps": [
                { "action": "click", "point": { "x": 540, "y": 1800 }, "anchor": "top-left" }
              ]
            }
        """.trimIndent()

        val script = parser.parse(json)
        assertEquals(540, script.steps[0].point?.x)
        assertEquals(1800, script.steps[0].point?.y)
    }

    @Test
    fun toJson_roundTrip() {
        val json = """
            {"name":"test","repeat":1,"steps":[{"action":"back"}]}
        """.trimIndent()
        val script = parser.parse(json)
        val result = parser.toJson(script)
        val reparsed = parser.parse(result)
        assertEquals(script.name, reparsed.name)
        assertEquals(script.steps.size, reparsed.steps.size)
    }

    @Test
    fun validate_validScript_noErrors() {
        val script = Script(
            name = "test",
            repeat = 1,
            steps = listOf(
                Step(action = "launchApp", packageName = "com.example.app"),
                Step(action = "back")
            )
        )
        val errors = parser.validate(script)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun validate_clickWithoutTarget_returnsError() {
        val script = Script(
            name = "test",
            repeat = 1,
            steps = listOf(Step(action = "click"))
        )
        val errors = parser.validate(script)
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("click") })
    }

    @Test
    fun validate_unknownAction_returnsError() {
        val script = Script(
            name = "test",
            repeat = 1,
            steps = listOf(Step(action = "flying"))
        )
        val errors = parser.validate(script)
        assertTrue(errors.any { it.contains("flying") })
    }
}
