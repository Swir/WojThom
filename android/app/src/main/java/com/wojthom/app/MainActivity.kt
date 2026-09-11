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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wojthom.app.data.AppStorage
import com.wojthom.app.i18n.AppLanguage
import com.wojthom.app.i18n.UiStrings
import com.wojthom.app.i18n.strings
import com.wojthom.app.model.HistorySnapshot
import com.wojthom.app.model.TimeEntry
import com.wojthom.app.parser.TimeParser
import com.wojthom.app.pdf.PdfExporter
import com.wojthom.app.stats.StatsCalculator
import com.wojthom.app.ui.theme.WojThomTheme
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsPrefs = getSharedPreferences("wojthom_settings", MODE_PRIVATE)
        val storage = AppStorage(this)

        setContent {
            var language by remember {
                mutableStateOf(AppLanguage.fromCode(settingsPrefs.getString("app_language", "pl")))
            }
            var darkTheme by remember { mutableStateOf(settingsPrefs.getBoolean("dark_theme", true)) }

            WojThomTheme(darkTheme = darkTheme) {
                WojThomApp(
                    storage = storage,
                    language = language,
                    darkTheme = darkTheme,
                    onLanguageChange = { newLanguage ->
                        language = newLanguage
                        settingsPrefs.edit().putString("app_language", newLanguage.code).apply()
                    },
                    onThemeChange = { newDarkTheme ->
                        darkTheme = newDarkTheme
                        settingsPrefs.edit().putBoolean("dark_theme", newDarkTheme).apply()
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
    storage: AppStorage,
    language: AppLanguage,
    darkTheme: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val ui = strings(language)
    val defaultHeaders = remember { setOf("Lista Czasu Pracy", "Work Time List", "Arbeidstidsliste") }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var header by remember { mutableStateOf(storage.loadHeader(ui.defaultHeader)) }
    var logs by remember { mutableStateOf(storage.loadLogs()) }
    var entries by remember { mutableStateOf(storage.loadEntries()) }
    var history by remember { mutableStateOf(storage.loadHistory()) }
    var statsEntries by remember { mutableStateOf(storage.loadStatsEntries()) }
    var showSystemMenu by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ui.defaultHeader) {
        if (header in defaultHeaders) {
            header = ui.defaultHeader
            storage.saveHeader(header)
        }
    }

    val tabs = listOf(
        AppTab(ui.work, Icons.Default.Home),
        AppTab(ui.history, Icons.Default.History),
        AppTab(ui.statistics, Icons.Default.BarChart),
        AppTab(ui.settings, Icons.Default.Settings)
    )

    fun updateEntries(value: List<TimeEntry>) {
        entries = value
        storage.saveEntries(value)
    }

    fun saveSnapshot() {
        if (entries.isEmpty()) return
        val snapshot = HistorySnapshot(
            header = header.ifBlank { ui.defaultHeader },
            totalMinutes = entries.sumOf { it.minutes },
            entries = entries
        )
        history = history + snapshot
        storage.saveHistory(history)
        statsEntries = storage.mergeStats(statsEntries, entries)
        storage.saveStatsEntries(statsEntries)
        Toast.makeText(context, ui.historySaved, Toast.LENGTH_SHORT).show()
    }

    fun clearAllWorkData() {
        storage.clearWorkData()
        header = ui.defaultHeader
        logs = ""
        entries = emptyList()
        history = emptyList()
        statsEntries = emptyList()
        selectedTab = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "WOJTHOM APEX",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "6.0 • ${ui.workTimeStudio}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSystemMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = ui.system)
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
                header = header,
                onHeaderChange = {
                    header = it
                    storage.saveHeader(it)
                },
                logs = logs,
                onLogsChange = {
                    logs = it
                    storage.saveLogs(it)
                },
                entries = entries,
                onEntriesChange = ::updateEntries,
                onSaveHistory = ::saveSnapshot,
                modifier = Modifier.padding(padding)
            )

            1 -> HistoryScreen(
                ui = ui,
                language = language,
                history = history,
                onLoad = { snapshot ->
                    header = snapshot.header
                    logs = snapshot.entries.joinToString("\n") { entry ->
                        entry.source.ifBlank {
                            buildString {
                                append(entry.date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "")
                                append(" ")
                                append(entry.client)
                                append(" ")
                                if (entry.start != "-" && entry.end != "-") {
                                    append("${entry.start} - ${entry.end}")
                                } else {
                                    append(entry.durationText)
                                }
                            }.trim()
                        }
                    }
                    updateEntries(snapshot.entries)
                    storage.saveHeader(header)
                    storage.saveLogs(logs)
                    selectedTab = 0
                },
                onDelete = { snapshot ->
                    history = history.filterNot { it.id == snapshot.id }
                    storage.saveHistory(history)
                },
                onClearHistory = {
                    history = emptyList()
                    storage.saveHistory(history)
                },
                modifier = Modifier.padding(padding)
            )

            2 -> StatisticsScreen(
                ui = ui,
                language = language,
                statsEntries = statsEntries,
                reportCount = history.size,
                onClear = {
                    statsEntries = emptyList()
                    storage.saveStatsEntries(statsEntries)
                },
                modifier = Modifier.padding(padding)
            )

            else -> SettingsScreen(
                ui = ui,
                language = language,
                darkTheme = darkTheme,
                onLanguageChange = onLanguageChange,
                onThemeChange = onThemeChange,
                onClearAll = { showClearAllDialog = true },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showSystemMenu) {
        AlertDialog(
            onDismissRequest = { showSystemMenu = false },
            title = { Text("WojThom 6.0 APEX") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(ui.aboutText)
                    HorizontalDivider()
                    OutlinedButton(
                        onClick = {
                            selectedTab = 1
                            showSystemMenu = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(ui.history) }
                    OutlinedButton(
                        onClick = {
                            selectedTab = 2
                            showSystemMenu = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(ui.statistics) }
                    OutlinedButton(
                        onClick = {
                            selectedTab = 3
                            showSystemMenu = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(ui.settings) }
                    FilledTonalButton(
                        onClick = { onThemeChange(!darkTheme) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (darkTheme) ui.lightTheme else ui.darkTheme) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSystemMenu = false }) { Text(ui.close) }
            }
        )
    }

    if (showClearAllDialog) {
        ConfirmDialog(
            title = ui.clearAllData,
            message = ui.clearAllQuestion,
            ui = ui,
            onConfirm = {
                showClearAllDialog = false
                clearAllWorkData()
            },
            onDismiss = { showClearAllDialog = false }
        )
    }
}

@Composable
private fun WorkScreen(
    ui: UiStrings,
    language: AppLanguage,
    header: String,
    onHeaderChange: (String) -> Unit,
    logs: String,
    onLogsChange: (String) -> Unit,
    entries: List<TimeEntry>,
    onEntriesChange: (List<TimeEntry>) -> Unit,
    onSaveHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var showPdfLanguageDialog by remember { mutableStateOf(false) }
    var pendingPdfLanguage by remember { mutableStateOf(language) }
    var editingEntry by remember { mutableStateOf<TimeEntry?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

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
            Toast.makeText(context, if (success) ui.pdfSaved else ui.pdfSaveError, Toast.LENGTH_SHORT).show()
        }
    }

    val filteredEntries = remember(entries, query) {
        val needle = query.trim().lowercase()
        if (needle.isBlank()) entries else entries.filter { entry ->
            entry.client.lowercase().contains(needle) ||
                entry.date?.toString()?.contains(needle) == true ||
                entry.source.lowercase().contains(needle)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(2.dp)) }
        item { WorkHero(entries = entries, ui = ui) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ui.newList, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(ui.dashboard, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("APEX", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }

                    OutlinedTextField(
                        value = header,
                        onValueChange = onHeaderChange,
                        label = { Text(ui.header) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = logs,
                        onValueChange = onLogsChange,
                        label = { Text(ui.pasteHours) },
                        supportingText = { Text(ui.hoursExample) },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { onEntriesChange(TimeParser.parse(logs)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(ui.generateList)
                    }

                    if (entries.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onSaveHistory,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Text("  ${ui.saveHistory}", maxLines = 1)
                            }
                            FilledTonalButton(
                                onClick = { showPdfLanguageDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Text("  PDF")
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        enabled = entries.isNotEmpty() || logs.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(ui.clear) }
                }
            }
        }

        if (entries.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text(ui.searchEntries) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (entries.isEmpty()) {
            item { EmptyPanel(ui.noEntries) }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${ui.entries} (${filteredEntries.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${entries.count { it.isValid }}/${entries.size} ${ui.validEntries.lowercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(filteredEntries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    ui = ui,
                    onEdit = { editingEntry = entry },
                    onDelete = { onEntriesChange(entries.filterNot { it.id == entry.id }) }
                )
            }
        }

        item { Spacer(Modifier.height(18.dp)) }
    }

    if (showPdfLanguageDialog) {
        PdfLanguageDialog(
            ui = ui,
            onDismiss = { showPdfLanguageDialog = false },
            onSelect = { pdfLanguage ->
                pendingPdfLanguage = pdfLanguage
                showPdfLanguageDialog = false
                createPdfLauncher.launch(strings(pdfLanguage).pdfFileName)
            }
        )
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            ui = ui,
            onDismiss = { editingEntry = null },
            onSave = { edited ->
                onEntriesChange(entries.map { if (it.id == edited.id) edited else it })
                editingEntry = null
            }
        )
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = ui.clear,
            message = ui.deleteQuestion,
            ui = ui,
            onConfirm = {
                onEntriesChange(emptyList())
                onLogsChange("")
                query = ""
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

@Composable
private fun WorkHero(entries: List<TimeEntry>, ui: UiStrings) {
    val total = entries.sumOf { it.minutes }
    val valid = entries.count { it.isValid }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(ui.totalTime.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                formatMinutes(total),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTiny(ui.entries, entries.size.toString())
                StatTiny(ui.validEntries, valid.toString())
                StatTiny("PDF", "PL/EN/NO")
            }
        }
    }
}

@Composable
private fun StatTiny(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EntryCard(entry: TimeEntry, ui: UiStrings, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isValid) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.74f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    entry.client.ifBlank { ui.missingClient },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(entry.date?.format(dateFormatter) ?: ui.invalidDate, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (entry.start != "-" && entry.end != "-") {
                        "${entry.start} – ${entry.end}  •  ${entry.durationText} h"
                    } else {
                        "${ui.workTime}: ${entry.durationText} h"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (!entry.isValid) {
                    Text(ui.needsCorrection, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = ui.edit) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = ui.delete) }
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    ui: UiStrings,
    language: AppLanguage,
    history: List<HistorySnapshot>,
    onLoad: (HistorySnapshot) -> Unit,
    onDelete: (HistorySnapshot) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<HistorySnapshot?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var pdfTarget by remember { mutableStateOf<HistorySnapshot?>(null) }
    var pendingPdfLanguage by remember { mutableStateOf(language) }
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val snapshot = pdfTarget
        if (uri != null && snapshot != null) {
            val success = PdfExporter.write(
                resolver = context.contentResolver,
                uri = uri,
                header = snapshot.header,
                entries = snapshot.entries,
                language = pendingPdfLanguage
            )
            Toast.makeText(context, if (success) ui.pdfSaved else ui.pdfSaveError, Toast.LENGTH_SHORT).show()
        }
        pdfTarget = null
    }

    val filtered = remember(history, query) {
        val needle = query.trim().lowercase()
        history.asReversed().filter { snapshot ->
            needle.isBlank() || snapshot.header.lowercase().contains(needle) ||
                snapshot.entries.any { it.client.lowercase().contains(needle) }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(2.dp)) }
        item {
            SectionHeader(ui.history, ui.historyDescription, history.size.toString())
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text(ui.searchHistory) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (history.isNotEmpty()) {
            item {
                OutlinedButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(ui.clearHistory)
                }
            }
        }
        if (filtered.isEmpty()) {
            item { EmptyPanel(ui.historyEmpty) }
        } else {
            items(filtered, key = { it.id }) { snapshot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(snapshot.header, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${ui.savedAt}: ${formatter.format(Date(snapshot.savedAt))}", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${ui.entries}: ${snapshot.entries.size}")
                            Text(formatMinutes(snapshot.totalMinutes), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(onClick = { onLoad(snapshot) }, modifier = Modifier.weight(1f)) { Text(ui.load) }
                            FilledTonalButton(onClick = { pdfTarget = snapshot }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Text("  PDF")
                            }
                            IconButton(onClick = { deleteTarget = snapshot }) {
                                Icon(Icons.Default.Delete, contentDescription = ui.delete)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }

    if (pdfTarget != null) {
        PdfLanguageDialog(
            ui = ui,
            onDismiss = { pdfTarget = null },
            onSelect = { selected ->
                pendingPdfLanguage = selected
                createPdfLauncher.launch(strings(selected).pdfFileName)
            }
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = ui.delete,
            message = ui.deleteQuestion,
            ui = ui,
            onConfirm = {
                onDelete(target)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = ui.clearHistory,
            message = ui.clearHistoryQuestion,
            ui = ui,
            onConfirm = {
                onClearHistory()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

@Composable
private fun StatisticsScreen(
    ui: UiStrings,
    language: AppLanguage,
    statsEntries: List<TimeEntry>,
    reportCount: Int,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = remember(statsEntries) { StatsCalculator.calculate(statsEntries) }
    var showClearDialog by remember { mutableStateOf(false) }
    val locale = when (language) {
        AppLanguage.POLISH -> Locale("pl", "PL")
        AppLanguage.ENGLISH -> Locale.ENGLISH
        AppLanguage.NORWEGIAN -> Locale("nb", "NO")
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(2.dp)) }
        item { SectionHeader(ui.statistics, ui.statisticsDescription, stats.entryCount.toString()) }

        if (stats.entryCount == 0) {
            item { EmptyPanel(ui.noStatistics) }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(ui.weeklyTarget.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(formatMinutes(stats.thisWeekMinutes), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        LinearProgressIndicator(
                            progress = { stats.weeklyProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        val diff = stats.weeklyDifferenceMinutes
                        Text(
                            when {
                                diff == 0 -> ui.targetReached
                                diff < 0 -> "${ui.remaining}: ${formatMinutes(-diff)}"
                                else -> "${ui.overTarget}: ${formatMinutes(diff)}"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(ui.thisMonth, formatMinutes(stats.thisMonthMinutes), Modifier.weight(1f))
                    MetricCard(ui.allTime, formatMinutes(stats.totalMinutes), Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(ui.reports, reportCount.toString(), Modifier.weight(1f))
                    MetricCard(ui.clients, stats.clientCount.toString(), Modifier.weight(1f))
                }
            }

            item {
                Text(ui.monthlyBreakdown, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(stats.months, key = { it.month.toString() }) { month ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val monthName = month.month.month.getDisplayName(TextStyle.FULL, locale)
                            Text("${monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }} ${month.month.year}", fontWeight = FontWeight.Bold)
                            Text("${ui.entries}: ${month.entries}", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(formatMinutes(month.minutes), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }
                }
            }
            item {
                OutlinedButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(ui.clearStatistics)
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = ui.clearStatistics,
            message = ui.clearStatisticsQuestion,
            ui = ui,
            onConfirm = {
                onClear()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SettingsScreen(
    ui: UiStrings,
    language: AppLanguage,
    darkTheme: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(2.dp)) }
        item { SectionHeader(ui.settings, ui.settingsDescription, "6.0") }
        item {
            SettingsCard(ui.applicationLanguage, ui.applicationLanguageDescription) {
                AppLanguage.entries.forEach { item ->
                    if (item == language) {
                        Button(onClick = { onLanguageChange(item) }, modifier = Modifier.fillMaxWidth()) {
                            Text("✓ ${item.nativeLabel}")
                        }
                    } else {
                        OutlinedButton(onClick = { onLanguageChange(item) }, modifier = Modifier.fillMaxWidth()) {
                            Text(item.nativeLabel)
                        }
                    }
                }
            }
        }
        item {
            SettingsCard(ui.theme, ui.themeDescription) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (darkTheme) {
                        Button(onClick = { onThemeChange(true) }, modifier = Modifier.weight(1f)) { Text("✓ ${ui.darkTheme}") }
                        OutlinedButton(onClick = { onThemeChange(false) }, modifier = Modifier.weight(1f)) { Text(ui.lightTheme) }
                    } else {
                        OutlinedButton(onClick = { onThemeChange(true) }, modifier = Modifier.weight(1f)) { Text(ui.darkTheme) }
                        Button(onClick = { onThemeChange(false) }, modifier = Modifier.weight(1f)) { Text("✓ ${ui.lightTheme}") }
                    }
                }
            }
        }
        item {
            SettingsCard(ui.data, ui.localDataDescription) {
                Text(ui.pdfLanguageNote, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onClearAll, modifier = Modifier.fillMaxWidth()) { Text(ui.clearAllData) }
                Text(ui.clearAllDataDescription, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SettingsCard(ui.about, ui.aboutText) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(ui.appVersion)
                    Text("6.0 APEX", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun SettingsCard(title: String, description: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun SectionHeader(title: String, description: String, badge: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.fillMaxWidth(0.78f)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(badge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun EmptyPanel(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text,
            modifier = Modifier.padding(22.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PdfLanguageDialog(
    ui: UiStrings,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ui.pdfLanguageTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(ui.pdfLanguageDescription)
                AppLanguage.entries.forEach { pdfLanguage ->
                    OutlinedButton(onClick = { onSelect(pdfLanguage) }, modifier = Modifier.fillMaxWidth()) {
                        Text(pdfLanguage.nativeLabel)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(ui.cancel) } }
    )
}

@Composable
private fun EditEntryDialog(
    entry: TimeEntry,
    ui: UiStrings,
    onDismiss: () -> Unit,
    onSave: (TimeEntry) -> Unit
) {
    var client by remember(entry.id) { mutableStateOf(entry.client) }
    var duration by remember(entry.id) { mutableStateOf(entry.durationText) }
    val parsedMinutes = parseDurationInput(duration)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ui.editEntry) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text(ui.clientCompany) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(ui.duration) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = client.isNotBlank() && parsedMinutes > 0,
                onClick = {
                    onSave(
                        entry.copy(
                            client = client.trim(),
                            start = "-",
                            end = "-",
                            minutes = parsedMinutes
                        )
                    )
                }
            ) { Text(ui.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(ui.cancel) } }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    ui: UiStrings,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(ui.confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(ui.cancel) } }
    )
}

private fun formatMinutes(minutes: Int): String = "%d:%02d h".format(minutes / 60, minutes % 60)

private fun parseDurationInput(value: String): Int {
    val text = value.trim().replace(',', '.')
    val clock = Regex("""^(\d{1,3}):([0-5]\d)$""").matchEntire(text)
    if (clock != null) {
        return clock.groupValues[1].toInt() * 60 + clock.groupValues[2].toInt()
    }
    val decimal = text.toDoubleOrNull() ?: return 0
    if (decimal <= 0 || decimal > 24) return 0
    return kotlin.math.round(decimal * 60).toInt()
}
