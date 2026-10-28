package com.turbomaster.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "About & Diagnostics", style = MaterialTheme.typography.titleLarge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Product Principles", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Turbo is built for real performance management without root or deceptive boosters. Every operation has a transparent, technically valid implementation:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = "• Zero fake RAM cleaning or CPU cooling animations.")
                Text(text = "• Shizuku execution uses argument tokens, avoiding shell injection.")
                Text(text = "• Critical system packages cannot be killed.")
                Text(text = "• Privileged actions are reversible.")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Security Allowlist", style = MaterialTheme.typography.titleMedium)
                Text(text = "Permitted binaries: am, cmd, settings, dumpsys, sm")
                Text(
                    text = "Forbidden binaries: su, setenforce, rm, dd, reboot, mount, chmod",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
