package com.turbomaster.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.turbomaster.app.data.model.ShizukuConnectionState

@Composable
fun ShizukuScreen(
    shizukuState: ShizukuConnectionState,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // State Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (shizukuState) {
                    is ShizukuConnectionState.Connected -> MaterialTheme.colorScheme.primaryContainer
                    is ShizukuConnectionState.PermissionRequired -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (shizukuState) {
                            is ShizukuConnectionState.Connected -> Icons.Default.CheckCircle
                            is ShizukuConnectionState.PermissionRequired -> Icons.Default.Warning
                            else -> Icons.Default.Cancel
                        },
                        contentDescription = null,
                        tint = when (shizukuState) {
                            is ShizukuConnectionState.Connected -> MaterialTheme.colorScheme.primary
                            is ShizukuConnectionState.PermissionRequired -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (shizukuState) {
                            is ShizukuConnectionState.Connected -> "Shizuku Connected & Authorized"
                            is ShizukuConnectionState.PermissionRequired -> "Permission Required"
                            is ShizukuConnectionState.ServiceStopped -> "Service Stopped"
                            is ShizukuConnectionState.BinderDead -> "Binder Died"
                            is ShizukuConnectionState.NotInstalled -> "Shizuku Not Installed"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (shizukuState) {
                        is ShizukuConnectionState.Connected -> "Running with privileged shell UID ${shizukuState.uid}. Ready for execution."
                        is ShizukuConnectionState.PermissionRequired -> "Turbo has requested permission to communicate with Shizuku. Tap below to grant."
                        is ShizukuConnectionState.ServiceStopped -> "The Shizuku app is installed, but the background service has not been started."
                        is ShizukuConnectionState.BinderDead -> "The Shizuku binder process was terminated by Android or crashed."
                        is ShizukuConnectionState.NotInstalled -> "Please install Shizuku to unlock rootless privileged management."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (shizukuState is ShizukuConnectionState.PermissionRequired) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Open Shizuku App Button
        OutlinedButton(
            onClick = {
                val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"))
                    context.startActivity(playStoreIntent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Shizuku Application")
        }

        // Wireless Debugging Setup Guide (Android 11+)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Wireless Debugging Setup Guide", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "On Android 11 to 15, you can start Shizuku directly on your device without a computer using Wireless Debugging:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val steps = listOf(
                    "Enable Developer Options (Settings > About Phone > Tap 'Build Number' 7 times).",
                    "Open Developer Options and turn on 'Wireless Debugging'.",
                    "Tap 'Pair device with pairing code' to view the 6-digit code and port.",
                    "Open the Shizuku app and tap 'Pairing', then enter the 6-digit code from notification.",
                    "Return to Shizuku and tap 'Start' to begin the service.",
                    "Return to Turbo and tap 'Grant Permission'."
                )

                steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${index + 1}. ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = step, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Reboot Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Notice on Device Reboot", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due to Android's security architecture, wireless debugging and Shizuku must be re-started after the phone is rebooted on non-rooted devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
