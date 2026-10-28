package com.turbomaster.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turbomaster.app.data.model.DeviceMetrics
import com.turbomaster.app.data.model.OperationRecord
import com.turbomaster.app.data.model.ShizukuConnectionState

@Composable
fun HomeScreen(
    metrics: DeviceMetrics?,
    shizukuState: ShizukuConnectionState,
    recentHistory: List<OperationRecord>,
    onNavigateToShizuku: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToActions: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (metrics != null) "${metrics.manufacturer} ${metrics.model}" else "Android Device",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (metrics != null) "${metrics.androidVersion} (API ${metrics.sdkInt}) · ${metrics.cpuAbi}" else "Reading specs...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh telemetry")
                    }
                }
            }
        }

        // Shizuku Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (shizukuState) {
                    is ShizukuConnectionState.Connected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            onClick = onNavigateToShizuku
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (shizukuState) {
                        is ShizukuConnectionState.Connected -> Icons.Default.CheckCircle
                        is ShizukuConnectionState.PermissionRequired -> Icons.Default.Warning
                        else -> Icons.Default.LinkOff
                    },
                    contentDescription = null,
                    tint = when (shizukuState) {
                        is ShizukuConnectionState.Connected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shizuku Integration",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = when (shizukuState) {
                            is ShizukuConnectionState.Connected -> "Authorized (v${shizukuState.version}, UID ${shizukuState.uid})"
                            is ShizukuConnectionState.PermissionRequired -> "Permission Required - Tap to Grant"
                            is ShizukuConnectionState.ServiceStopped -> "Service Stopped - Tap to Configure"
                            is ShizukuConnectionState.BinderDead -> "Binder Crashed - Reconnect Needed"
                            is ShizukuConnectionState.NotInstalled -> "Not Installed"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        // Real Telemetry Row
        if (metrics != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // RAM Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "RAM Available", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${metrics.availableRamMb} MB",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { metrics.ramUsedPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total ${metrics.totalRamMb} MB (${metrics.ramUsedPercent}% used)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Storage Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Storage Free", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%.1f GB".format(metrics.availableStorageGb),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val storageUsedPercent = if (metrics.totalStorageGb > 0) {
                            ((metrics.totalStorageGb - metrics.availableStorageGb) / metrics.totalStorageGb).toFloat()
                        } else 0f
                        LinearProgressIndicator(
                            progress = { storageUsedPercent },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total %.1f GB".format(metrics.totalStorageGb),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Battery Telemetry
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (metrics.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Battery ${metrics.batteryPercent}%", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (metrics.isCharging) "Charging via AC/USB" else "Discharging · Health Normal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Shortcuts
        Text(text = "Management Actions", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToApps,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Apps, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Apps")
            }
            OutlinedButton(
                onClick = onNavigateToActions,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Turbo Tools")
            }
        }

        // Recent Operations Preview
        if (recentHistory.isNotEmpty()) {
            Text(text = "Recent Operations", style = MaterialTheme.typography.titleMedium)
            recentHistory.take(3).forEach { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (record.isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (record.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = record.actionName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = record.outputMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = record.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
