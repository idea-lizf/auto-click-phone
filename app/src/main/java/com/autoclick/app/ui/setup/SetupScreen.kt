package com.autoclick.app.ui.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoclick.app.ui.PermissionChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen() {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }

    val permissions = remember(refreshKey) {
        listOf(
            PermissionItem(
                title = "无障碍服务",
                description = "核心功能必需，用于读取屏幕节点和派发手势",
                granted = PermissionChecker.isAccessibilityEnabled(context),
                intent = PermissionChecker.accessibilitySettingsIntent()
            ),
            PermissionItem(
                title = "悬浮窗权限",
                description = "M3 悬浮球控制必需，当前用于前台服务保活",
                granted = PermissionChecker.isOverlayEnabled(context),
                intent = PermissionChecker.overlaySettingsIntent(context)
            ),
            PermissionItem(
                title = "电池优化白名单",
                description = "防止系统杀后台导致长脚本中断",
                granted = PermissionChecker.isBatteryOptimizationIgnored(context),
                intent = PermissionChecker.batterySettingsIntent(context)
            )
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("权限设置") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "请依次开启以下权限，确保脚本正常运行。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            items(permissions) { item ->
                PermissionCard(item) {
                    context.startActivity(item.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    refreshKey++
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                if (permissions.all { it.granted }) {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )) {
                        Text(
                            text = "所有权限已就绪，可以运行脚本了",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private data class PermissionItem(
    val title: String,
    val description: String,
    val granted: Boolean,
    val intent: Intent
)

@Composable
private fun PermissionCard(
    item: PermissionItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (item.granted) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (item.granted) "已开启" else "未开启，点击设置",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.granted) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.width(8.dp))
            if (!item.granted) {
                TextButton(onClick = onClick) { Text("去设置") }
            }
        }
    }
}
