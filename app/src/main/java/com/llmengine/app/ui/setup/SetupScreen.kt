package com.llmengine.app.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llmengine.app.data.model.DownloadState
import com.llmengine.app.data.model.ModelInfo
import com.llmengine.app.data.model.ModelRegistry
import com.llmengine.app.data.model.ModelType
import com.llmengine.app.data.preferences.SecurePreferences
import com.llmengine.app.download.ModelDownloadManager
import kotlinx.coroutines.launch

/**
 * Setup screen shown on first launch.
 * Guides the user through downloading required models.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { SecurePreferences(context) }
    val downloadManager = remember { ModelDownloadManager(context) }
    val scope = rememberCoroutineScope()

    // Check if setup was already completed
    LaunchedEffect(Unit) {
        if (prefs.isSetupComplete()) {
            onSetupComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to LLM Answer Engine") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Download models to get started",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Models run completely on your device. No data is sent to external servers for inference.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(ModelRegistry.availableModels) { model ->
                ModelDownloadCard(
                    model = model,
                    downloadManager = downloadManager,
                    onDownload = {
                        scope.launch {
                            downloadManager.downloadModel(model)
                        }
                    },
                    onPause = { downloadManager.pauseDownload(model.id) },
                    onCancel = { downloadManager.cancelDownload(model.id) }
                )
            }

            item {
                val allDownloaded = ModelRegistry.availableModels.all { model ->
                    downloadManager.isModelDownloaded(model.id)
                }

                Button(
                    onClick = {
                        prefs.setSetupComplete(true)
                        onSetupComplete()
                    },
                    enabled = allDownloaded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = if (allDownloaded) "Continue" else "Download all models to continue",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Allow skipping for development
                TextButton(
                    onClick = {
                        prefs.setSetupComplete(true)
                        onSetupComplete()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now (limited functionality)")
                }
            }
        }
    }
}

@Composable
fun ModelDownloadCard(
    model: ModelInfo,
    downloadManager: ModelDownloadManager,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onCancel: () -> Unit
) {
    val downloadState by downloadManager.getDownloadState(model.id).collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when (model.type) {
                        ModelType.LLM -> Icons.Default.CloudDownload
                        ModelType.EMBEDDING -> Icons.Default.CloudDownload
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (model.type) {
                            ModelType.LLM -> "Language Model"
                            ModelType.EMBEDDING -> "Embedding Model"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = downloadManager.getModelSizeFormatted(model.sizeBytes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = downloadState) {
                is DownloadState.NotDownloaded -> {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download")
                    }
                }
                is DownloadState.Downloading -> {
                    LinearProgressIndicator(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${downloadManager.getModelSizeFormatted(state.downloadedBytes)} / ${downloadManager.getModelSizeFormatted(state.totalBytes)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) {
                            Text("Pause")
                        }
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                    }
                }
                is DownloadState.Paused -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onDownload, modifier = Modifier.weight(1f)) {
                            Text("Resume")
                        }
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                    }
                }
                is DownloadState.Verifying -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying checksum…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is DownloadState.Downloaded -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Downloaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is DownloadState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
