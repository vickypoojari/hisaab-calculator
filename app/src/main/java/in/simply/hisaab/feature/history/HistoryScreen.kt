package `in`.simply.hisaab.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.simply.hisaab.R
import `in`.simply.hisaab.core.designsystem.HisaabDimens
import `in`.simply.hisaab.core.designsystem.LocalHisaabTypeScale
import `in`.simply.hisaab.core.designsystem.toTextStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class HistoryActions(
    val onBack: () -> Unit,
    val onReuse: (HistoryEntry) -> Unit,
    val onCopy: (HistoryEntry) -> Unit,
    val onDelete: (HistoryEntry) -> Unit,
    val onClearAll: () -> Unit,
)

/** Full-screen list grouped by day. Tap reuses the result; long-press offers Copy and Delete. */
@Composable
fun HistoryScreen(sections: List<HistorySection>, actions: HistoryActions, modifier: Modifier = Modifier) {
    var confirmClear by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(showClear = sections.isNotEmpty(), onBack = actions.onBack, onClearAll = { confirmClear = true })
        if (sections.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.history_empty),
                    style = LocalHisaabTypeScale.current.bodyL.toTextStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                sections.forEach { section ->
                    item(key = "header-${section.day}") { SectionHeader(section.day, section.entries.first().createdAt) }
                    items(section.entries, key = { it.id }) { entry -> HistoryRow(entry, actions) }
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = { Text(stringResource(R.string.history_clear_body)) },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; actions.onClearAll() }) { Text(stringResource(R.string.history_clear_confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.history_cancel)) } },
        )
    }
}

@Composable
private fun TopBar(showClear: Boolean, onBack: () -> Unit, onClearAll: () -> Unit) {
    val minTarget = HisaabDimens.MinTouchTargetDp.dp
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(minTarget)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.history_back))
        }
        Text(
            stringResource(R.string.history_title),
            style = LocalHisaabTypeScale.current.title.toTextStyle(),
            modifier = Modifier.weight(1f).padding(start = 4.dp).semantics { heading() },
        )
        if (showClear) {
            TextButton(onClick = onClearAll, modifier = Modifier.heightIn(min = minTarget)) {
                Text(stringResource(R.string.history_clear_all), style = LocalHisaabTypeScale.current.body.toTextStyle())
            }
        }
    }
}

@Composable
private fun SectionHeader(day: DayLabel, firstEntryMillis: Long) {
    val label = when (day) {
        DayLabel.Today -> stringResource(R.string.history_today)
        DayLabel.Yesterday -> stringResource(R.string.history_yesterday)
        is DayLabel.Date -> formatDate(day, firstEntryMillis)
    }
    Text(
        label,
        style = LocalHisaabTypeScale.current.label.toTextStyle(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp).semantics { heading() },
    )
}

private fun formatDate(day: DayLabel.Date, fallbackMillis: Long): String {
    val calendar = GregorianCalendar(day.year, day.month - 1, day.day)
    val locale = Locale.getDefault()
    return SimpleDateFormat("d MMM yyyy", locale).format(calendar.time).uppercase(locale)
        .ifEmpty { fallbackMillis.toString() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(entry: HistoryEntry, actions: HistoryActions) {
    var menuOpen by remember { mutableStateOf(false) }
    val copy = stringResource(R.string.history_copy)
    val delete = stringResource(R.string.history_delete)
    Box {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .combinedClickable(onClick = { actions.onReuse(entry) }, onLongClick = { menuOpen = true })
                    .semantics {
                        customActions = listOf(
                            CustomAccessibilityAction(copy) { actions.onCopy(entry); true },
                            CustomAccessibilityAction(delete) { actions.onDelete(entry); true },
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(entry.expression, style = LocalHisaabTypeScale.current.body.toTextStyle(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (entry.source == HistorySource.SELECTED_TEXT) {
                        Text(
                            stringResource(R.string.history_from_selected_text),
                            style = LocalHisaabTypeScale.current.caption.toTextStyle(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    entry.result,
                    style = LocalHisaabTypeScale.current.title.toTextStyle(),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(text = { Text(copy) }, onClick = { menuOpen = false; actions.onCopy(entry) })
            DropdownMenuItem(text = { Text(delete) }, onClick = { menuOpen = false; actions.onDelete(entry) })
        }
    }
}
