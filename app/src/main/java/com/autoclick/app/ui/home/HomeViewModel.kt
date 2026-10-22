package com.autoclick.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclick.app.AutoClickApp
import com.autoclick.app.data.entity.ScriptEntity
import com.autoclick.app.engine.ScriptParser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AutoClickApp.instance.scriptRepository

    val scripts: StateFlow<List<ScriptEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun importDemoScript() {
        viewModelScope.launch {
            val json = getApplication<Application>().assets.open("demo_script.json")
                .bufferedReader().use { it.readText() }
            val script = ScriptParser.parse(json)
            repo.insert(
                ScriptEntity(
                    name = script.name,
                    targetPackage = script.targetPackage,
                    repeatCount = script.repeat,
                    json = json
                )
            )
        }
    }

    fun importFromJson(name: String, jsonString: String) {
        viewModelScope.launch {
            val script = ScriptParser.parse(jsonString)
            repo.insert(
                ScriptEntity(
                    name = name.ifBlank { script.name },
                    targetPackage = script.targetPackage,
                    repeatCount = script.repeat,
                    json = jsonString
                )
            )
        }
    }

    fun deleteScript(entity: ScriptEntity) {
        viewModelScope.launch { repo.delete(entity) }
    }
}
