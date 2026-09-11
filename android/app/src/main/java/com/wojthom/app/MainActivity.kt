package com.wojthom.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wojthom.app.i18n.AppLanguage
import com.wojthom.app.i18n.UiStrings
import com.wojthom.app.i18n.strings
import com.wojthom.app.model.TimeEntry
import com.wojthom.app.parser.TimeParser
import com.wojthom.app.pdf.PdfExporter
import com.wojthom.app.ui.theme.WojThomTheme
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = getSharedPreferences("wojthom_settings", MODE_PRIVATE)

        setContent {
            var language by remember {
                mutableStateOf(AppLanguage.fromCode(preferences.getString("app_language", "pl")))
            }

            WojThomTheme {
                WojThomApp(
                    language = language,
                    onLanguageChange = { newLanguage ->
                        language = newLanguage
                        preferences.edit().putString("app_language", newLanguage.code).apply()
                    }
                )
            }
        }
    }
}

private data class AppTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WojThomApp(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val ui = strings(language)
    val tabs = listOf(
        AppTab(ui.work, Icons.Default.Home),
        AppTab(ui.history, Icons.Default.History),
        AppTab(ui.statistics, Icons.Default.BarChart),
        AppTab(ui.settings, Icons.Default.Settings)
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WojThom", fontWeight = FontWeight.Bold)
                        Text("6.0 • ${ui.workTimeStudio}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> WorkScreen(
                ui = ui,
                language = language,
                modifier = Modifier.padding(padding)
            )
            1 -> PlaceholderScreen(
                title = ui.history,
                description = ui.historyDescription,
                footer = ui.modulePlaceholder,
                modifier = Modifier.padding(padding)
            )
            2 -> PlaceholderScreen(
                title = ui.statistics,
                description = ui.statisticsDescription,
                footer = ui.modulePlaceholder,
                modifier = Modifier.padding(padding)
            )
            else -> SettingsScreen(
                ui = ui,
                language = language,
                onLanguageChange = onLanguageChange,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun WorkScreen(
    ui: UiStrings,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultHeaders = remember {
        setOf("Lista Czasu Pracy", "Work Time List", "Arbeidstidsliste")
    }

    var header by rememberSaveable { mutableStateOf(ui.defaultHeader) }
    var logs by rememberSaveable { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<TimeEntry>>(emptyList()) }
    var showPdfLanguageDialog by remember { mutableStateOf(false) }
    var pendingPdfLanguage by remember { mutableStateOf(language) }

    LaunchedEffect(ui.defaultHeader) {
        if (header in defaultHeaders) {
            header = ui.defaultHeader
        }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            val success = PdfExporter.write(
                resolver = context.contentResolver,
                uri = uri,
                header = header,
                entries = entries,
                language = pendingPdfLanguage
            )
            Toast.makeText(
                context,
                if (success) ui.pdfSaved else ui.pdfSaveError,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val totalMinutes = entries.sumOf { it.minutes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            SummaryCard(totalMinutes, entries.size, ui)
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(ui.newList, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = header,
                        onValueChange = { header = it },
                        label = { Text(ui.header) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = logs,
                        onValueChange = { logs = it },
                        label = { Text(ui.pasteHours) },
                        supportingText = { Text(ui.hoursExample) },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(onClick = { entries = TimeParser.parse(logs) }) {
                            Text(ui.generateList)
                        }
                        OutlinedButton(
                            onClick = {
                                logs = ""
                                entries = emptyList()
                            }
                        ) {
                            Text(ui.clear)
                        }
                    }

                    if (entries.isNotEmpty()) {
                        Button(
                            onClick = { showPdfLanguageDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ui.exportPdf)
                        }
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        ui.noEntries,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            item {
                Text(
                    "${ui.entries} (${entries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(entries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    ui = ui,
                    onDelete = { entries = entries.filterNot { it.id == entry.id } }
                )
            }
        }

        item { Spacer(Modifier.height(18.dp)) }
    }

    if (showPdfLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showPdfLanguageDialog = false },
            title = { Text(ui.pdfLanguageTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(ui.pdfLanguageDescription)
                    AppLanguage.entries.forEach { pdfLanguage ->
                        OutlinedButton(
                            onClick = {
                                pendingPdfLanguage = pdfLanguage
                                showPdfLanguageDialog = false
                                createPdfLauncher.launch(strings(pdfLanguage).pdfFileName)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(pdfLanguage.nativeLabel)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPdfLanguageDialog = false }) {
                    Text(ui.cancel)
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(totalMinutes: Int, entryCount: Int, ui: UiStrings) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(ui.totalTime, style = MaterialTheme.typography.labelLarge)
                Text(
                    "%d:%02d h".format(totalMinutes / 60, totalMinutes % 60),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(ui.entries, style = MaterialTheme.typography.labelLarge)
                Text(
                    entryCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EntryCard(entry: TimeEntry, ui: UiStrings, onDelete: () -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.82f)) {
                Text(
                    entry.client.ifBlank { ui.missingClient },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(entry.date?.format(dateFormatter) ?: ui.invalidDate)
                Text(
                    if (entry.start != "-" && entry.end != "-") {
                        "${entry.start} – ${entry.end} • ${entry.durationText} h"
                    } else {
                        "${ui.workTime}: ${entry.durationText} h"
                    }
                )
                if (!entry.isValid) {
                    Text(
                        ui.needsCorrection,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = ui.delete)
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    ui: UiStrings,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Text(
                ui.settings,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(ui.settingsDescription, style = MaterialTheme.typography.bodyLarge)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        ui.applicationLanguage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(ui.applicationLanguageDescription)

                    AppLanguage.entries.forEach { item ->
                        if (item == language) {
                            Button(
                                onClick = { onLanguageChange(item) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✓ ${item.nativeLabel}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onLanguageChange(item) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(item.nativeLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    footer: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyLarge)
        Text(footer)
    }
}
