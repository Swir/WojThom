package com.wojthom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wojthom.app.model.TimeEntry
import com.wojthom.app.parser.TimeParser
import com.wojthom.app.ui.theme.WojThomTheme
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WojThomTheme {
                WojThomApp()
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
private fun WojThomApp() {
    val tabs = listOf(
        AppTab("Praca", Icons.Default.Home),
        AppTab("Historia", Icons.Default.History),
        AppTab("Statystyki", Icons.Default.BarChart),
        AppTab("Ustawienia", Icons.Default.Settings)
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WojThom", fontWeight = FontWeight.Bold)
                        Text("6.0 • Work Time Studio", style = MaterialTheme.typography.labelMedium)
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
            0 -> WorkScreen(Modifier.padding(padding))
            1 -> PlaceholderScreen("Historia", "Tu trafi archiwum zapisanych list.", Modifier.padding(padding))
            2 -> PlaceholderScreen("Statystyki", "Tu trafi analiza tygodni, miesięcy i normy 37,5 h.", Modifier.padding(padding))
            else -> PlaceholderScreen("Ustawienia", "Język, motyw, eksport i ustawienia aplikacji.", Modifier.padding(padding))
        }
    }
}

@Composable
private fun WorkScreen(modifier: Modifier = Modifier) {
    var header by rememberSaveable { mutableStateOf("Lista Czasu Pracy") }
    var logs by rememberSaveable { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<TimeEntry>>(emptyList()) }

    val totalMinutes = entries.sumOf { it.minutes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            SummaryCard(totalMinutes, entries.size)
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Nowa lista", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = header,
                        onValueChange = { header = it },
                        label = { Text("Nagłówek") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = logs,
                        onValueChange = { logs = it },
                        label = { Text("Wklej godziny pracy") },
                        supportingText = {
                            Text("Np. 15.02.2026 Firma A 08:00 - 16:00 • 16.02 Firma B 7.5h")
                        },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { entries = TimeParser.parse(logs) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Generuj listę")
                        }
                        OutlinedButton(
                            onClick = {
                                logs = ""
                                entries = emptyList()
                            }
                        ) {
                            Text("Wyczyść")
                        }
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "Brak wpisów. Wklej logi i wybierz „Generuj listę”.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            item {
                Text(
                    "Wpisy (${entries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(entries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    onDelete = { entries = entries.filterNot { it.id == entry.id } }
                )
            }
        }

        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun SummaryCard(totalMinutes: Int, entryCount: Int) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Łączny czas", style = MaterialTheme.typography.labelLarge)
                Text(
                    "%d:%02d h".format(totalMinutes / 60, totalMinutes % 60),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text("Wpisy", style = MaterialTheme.typography.labelLarge)
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
private fun EntryCard(entry: TimeEntry, onDelete: () -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.client.ifBlank { "Brak klienta" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(entry.date?.format(dateFormatter) ?: "Nieprawidłowa data")
                Text(
                    if (entry.start != "-" && entry.end != "-") {
                        "${entry.start} – ${entry.end} • ${entry.durationText} h"
                    } else {
                        "Czas pracy: ${entry.durationText} h"
                    }
                )
                if (!entry.isValid) {
                    Text(
                        "Wpis wymaga poprawy",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, description: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyLarge)
        Text("Moduł przygotowany do dalszego przenoszenia funkcji WojThom 5.x.")
    }
}
