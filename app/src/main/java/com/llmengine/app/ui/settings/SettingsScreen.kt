package com.llmengine.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.llmengine.app.data.model.AppSettings
import com.llmengine.app.data.model.MemoryMode
import com.llmengine.app.data.preferences.SecurePreferences

/**
 * Settings screen for configuring the app.
 * Includes Brave API key input, model parameters, and performance controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { SecurePreferences(context) }

    var settings by remember { mutableStateOf(prefs.loadSettings()) }
    var braveApiKey by remember { mutableStateOf(prefs.getBraveApiKey() ?: "") }
    var showApiKey by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSavedSnackbar) {
        if (showSavedSnackbar) {
            snackbarHostState.showSnackbar("Settings saved")
            showSavedSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        prefs.saveSettings(settings)
                        if (braveApiKey.isNotBlank()) {
                            prefs.saveBraveApiKey(braveApiKey)
                        } else {
                            prefs.clearBraveApiKey()
                        }
                        showSavedSnackbar = true
                    }) {
                        Text("Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Web Search Section ---
            SettingsSection(title = "Web Search") {
                Text(
                    text = "Provide your own Brave Search API key to enable web search augmented generation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = braveApiKey,
                    onValueChange = { braveApiKey = it },
                    label = { Text("Brave Search API Key") },
                    placeholder = { Text("Paste your API key here") },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enable Web Search",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.webSearchEnabled,
                        onCheckedChange = {
                            settings = settings.copy(webSearchEnabled = it)
                        },
                        enabled = braveApiKey.isNotBlank()
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "How to get a Brave API key:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Visit brave.com/search/api\n" +
                                "2. Create a free account\n" +
                                "3. Generate an API key\n" +
                                "4. Paste it above",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Generation Settings ---
            SettingsSection(title = "Generation") {
                // Max Tokens
                Text(
                    text = "Max Tokens: ${settings.maxTokens}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = settings.maxTokens.toFloat(),
                    onValueChange = { settings = settings.copy(maxTokens = it.toInt()) },
                    valueRange = 64f..2048f,
                    steps = 15
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature
                Text(
                    text = "Temperature: %.2f".format(settings.temperature),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = settings.temperature,
                    onValueChange = { settings = settings.copy(temperature = it) },
                    valueRange = 0f..2f
                )
            }

            // --- Performance Settings ---
            SettingsSection(title = "Performance") {
                // CPU Threads
                Text(
                    text = "CPU Threads: ${settings.cpuThreads}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = settings.cpuThreads.toFloat(),
                    onValueChange = { settings = settings.copy(cpuThreads = it.toInt()) },
                    valueRange = 1f..8f,
                    steps = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Memory Mode
                Text(
                    text = "Memory Usage Mode",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                MemoryMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = settings.memoryMode == mode,
                            onClick = { settings = settings.copy(memoryMode = mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = when (mode) {
                                    MemoryMode.LOW -> "512 token context, minimal memory usage"
                                    MemoryMode.BALANCED -> "2048 token context, balanced performance"
                                    MemoryMode.HIGH -> "4096 token context, maximum quality"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- Privacy Info ---
            SettingsSection(title = "Privacy") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Privacy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• All AI inference runs locally on your device\n" +
                                "• No conversation data is sent to external servers\n" +
                                "• Web search (if enabled) sends only search queries to Brave\n" +
                                "• Your API key is stored encrypted on device\n" +
                                "• You can delete all data at any time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
