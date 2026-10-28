package com.turbomaster.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turbomaster.app.data.model.AppItem

@Composable
fun AppManagerScreen(
    apps: List<AppItem>,
    isLoading: Boolean,
    onForceStop: (AppItem) -> Unit,
    onOpenDetails: (String) -> Unit,
    onFilterSystemChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppItem?>(null) }
    var appToConfirmForceStop by remember { mutableStateOf<AppItem?>(null) }

    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search application or package...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredApps.size} applications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Include System Apps", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = showSystemApps,
                    onCheckedChange = {
                        showSystemApps = it
                        onFilterSystemChange(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedApp = app },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar Icon Initial
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (app.isCriticalPackage) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = app.appName.firstOrNull()?.uppercase() ?: "A",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (app.isCriticalPackage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = app.appName, style = MaterialTheme.typography.bodyMedium)
                                    if (app.isCriticalPackage) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Protected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "SDK ${app.targetSdk}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // App Details Bottom Sheet / Dialog
    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text(app.appName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Package: ${app.packageName}")
                    Text("Version: ${app.versionName}")
                    Text("UID: ${app.uid}")
                    Text("Target SDK: ${app.targetSdk}")
                    Text("Type: ${if (app.isSystemApp) "System Application" else "User Installed"}")
                    if (app.isCriticalPackage) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠ This package is on the critical safety exclusion list and cannot be force-stopped to prevent device crash.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                if (!app.isCriticalPackage) {
                    Button(
                        onClick = {
                            appToConfirmForceStop = app
                            selectedApp = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Force Stop")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    onOpenDetails(app.packageName)
                    selectedApp = null
                }) {
                    Text("Open System Settings")
                }
            }
        )
    }

    // Force Stop Confirmation Dialog
    appToConfirmForceStop?.let { app ->
        AlertDialog(
            onDismissRequest = { appToConfirmForceStop = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirm Force Stop") },
            text = {
                Text("Are you sure you want to force-stop '${app.appName}'? Its background processes and tasks will be terminated immediately.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onForceStop(app)
                        appToConfirmForceStop = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Force Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { appToConfirmForceStop = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
