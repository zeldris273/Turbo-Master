package com.turbomaster.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdvancedScreen(
    onTuneAnimationScales: (Float) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationScale by remember { mutableStateOf(1.0f) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Advanced System Tuning", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Reversible parameters stored via Android Global Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {
                animationScale = 1.0f
                onResetDefaults()
            }) {
                Icon(Icons.Default.RotateLeft, contentDescription = "Reset Defaults")
            }
        }

        // Animation Scales Tuning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Window & Transition Animation Scale", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Reducing animation durations from 1.0x to 0.5x eliminates perceptual touch lag and reduces compositor overhead on budget processors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val scaleOptions = listOf(0.0f, 0.5f, 1.0f, 1.5f)
                    scaleOptions.forEach { scale ->
                        FilterChip(
                            selected = animationScale == scale,
                            onClick = {
                                animationScale = scale
                                onTuneAnimationScales(scale)
                            },
                            label = { Text("${scale}x") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Cached Apps Freezer (Android 11+)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Cached Apps Freezer (cgroup v2)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Freezes cached background processes using SIGSTOP to completely stop background CPU usage until re-opened by the user. Supported natively on Android 11+.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reset Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Rollback & Reset Guarantee", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Every tweak applied through Turbo preserves its initial state. You can restore default factory values at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        animationScale = 1.0f
                        onResetDefaults()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore Default System Settings")
                }
            }
        }
    }
}
