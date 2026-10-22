package com.autoclick.app.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoclick.app.data.entity.ScriptEntity
import com.autoclick.app.keepalive.EngineForegroundService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModel()
    val scripts by viewModel.scripts.collectAsState()
    val context = LocalContext.current

    var showImportDialog by remember { mutableStateOf(false) }

    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() }
            if (json != null) {
                val name = it.lastPathSegment?.substringAfterLast('/') ?: "导入脚本"
                viewModel.importFromJson(name, json)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("脚本列表") },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "导入")
                    }
                }
            )
        }
    ) { padding ->
        if (scripts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无脚本", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.importDemoScript() }) {
                        Text("导入 Demo 脚本")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scripts, key = { it.id }) { script ->
                    ScriptCard(
                        script = script,
                        onRun = {
                            EngineForegroundService.startScript(
                                context,
                                script.json,
                                script.id
                            )
                        },
                        onDelete = { viewModel.deleteScript(script) }
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入脚本") },
            text = { Text("选择来源：Demo 脚本可直接导入，或从文件选择 JSON。") },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    viewModel.importDemoScript()
                }) { Text("导入 Demo") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    openFileLauncher.launch(arrayOf("application/json", "*/*"))
                }) { Text("从文件选择") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScriptCard(
    script: ScriptEntity,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = script.name,
                    style = MaterialTheme.typography.titleMedium
                )
                script.targetPackage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = "重复 ${script.repeatCount} 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onRun) {
                Icon(Icons.Default.PlayArrow, contentDescription = "运行")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}
