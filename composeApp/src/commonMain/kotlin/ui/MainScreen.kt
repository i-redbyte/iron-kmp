package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import platform.PortItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState,
    strings: Strings,
    log: ConsoleLog,
    onRefresh: () -> Unit,
    onToggleAuto: (Boolean, Long) -> Unit,
    onSelectPort: (PortItem?) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit
) {
    val lines = log.lines.collectAsState().value
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.scrollToItem(lines.lastIndex)
    }

    val canConnect = state.selectedPort != null && state.connectionState == ConnectionState.DISCONNECTED
    val canDisconnect = state.connectionState != ConnectionState.DISCONNECTED
    val canSend = state.connectionState == ConnectionState.CONNECTED && state.inputText.trim().isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                strings.appTitle,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.weight(1f))
            LanguageSelector(
                locale = state.locale,
                strings = strings,
                onLocaleChange = onLocaleChange
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PortDropdown(
                ports = state.ports,
                selected = state.selectedPort,
                label = strings.device,
                emptyText = strings.portsEmpty,
                onSelect = onSelectPort,
                modifier = Modifier.weight(1f)
            )

            Button(
                enabled = canConnect,
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(strings.connect, fontFamily = FontFamily.Monospace) }

            OutlinedButton(
                enabled = canDisconnect,
                onClick = onDisconnect
            ) { Text(strings.disconnect, fontFamily = FontFamily.Monospace) }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onRefresh) {
                Text(strings.refresh, fontFamily = FontFamily.Monospace)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.autoRefresh,
                    onCheckedChange = { onToggleAuto(it, 2500L) }
                )
                Text(strings.autoRefresh, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                label = { Text(strings.inputHint, fontFamily = FontFamily.Monospace) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Button(
                enabled = canSend,
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(strings.send, fontFamily = FontFamily.Monospace) }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            strings.console,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(lines.size) { idx ->
                    val line = lines[idx]
                    Text(
                        "[${line.ts}] ${line.text}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortDropdown(
    ports: List<PortItem>,
    selected: PortItem?,
    label: String,
    emptyText: String,
    onSelect: (PortItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.title ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = FontFamily.Monospace) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (ports.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(emptyText, fontFamily = FontFamily.Monospace) },
                    onClick = { expanded = false }
                )
            } else {
                ports.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.title, fontFamily = FontFamily.Monospace) },
                        onClick = {
                            onSelect(p)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    locale: AppLocale,
    strings: Strings,
    onLocaleChange: (AppLocale) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(strings.language, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.width(8.dp))
        TextButton(
            onClick = { onLocaleChange(AppLocale.RU) },
            enabled = locale != AppLocale.RU
        ) { Text("RU", fontFamily = FontFamily.Monospace) }
        TextButton(
            onClick = { onLocaleChange(AppLocale.EN) },
            enabled = locale != AppLocale.EN
        ) { Text("EN", fontFamily = FontFamily.Monospace) }
    }
}
