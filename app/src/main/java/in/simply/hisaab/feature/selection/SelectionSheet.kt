package `in`.simply.hisaab.feature.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.simply.hisaab.R
import `in`.simply.hisaab.core.designsystem.HisaabDimens
import `in`.simply.hisaab.core.designsystem.LocalHisaabColors
import `in`.simply.hisaab.parser.ErrorReason

class SelectionActions(
    val onReplace: (String) -> Unit,
    val onCopy: (String) -> Unit,
    val onSelectReading: (Int) -> Unit,
    val onOpenApp: () -> Unit,
    val onClose: () -> Unit,
)

private val PillHeight = 56.dp
private val MinTarget = HisaabDimens.MinTouchTargetDp.dp

/**
 * The bottom sheet from the design board: handle, header, state body, privacy line (always last).
 * Edit expression and the lakh/crore word line are not in the skeleton yet.
 */
@Composable
fun SelectionSheet(state: SelectionUiState, actions: SelectionActions, modifier: Modifier = Modifier) {
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(max = maxHeight),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally).size(width = 32.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
            )
            Header(actions.onClose)
            when (state) {
                is SelectionUiState.Success -> SuccessBody(state, actions)
                is SelectionUiState.Ambiguous -> AmbiguousBody(state, actions)
                is SelectionUiState.NothingToCalculate -> NothingBody(state, actions)
                is SelectionUiState.Error -> ErrorBody(state, actions)
            }
            Text(
                text = stringResource(R.string.selection_privacy),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Header(onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.selection_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        IconButton(onClick = onClose, modifier = Modifier.size(MinTarget)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.selection_close))
        }
    }
}

@Composable
private fun SuccessBody(state: SelectionUiState.Success, actions: SelectionActions) {
    ReadAsCard(state.readAs, state.ignoredWords)
    Text(
        text = "= ${state.resultText}",
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    ResultActions(state.replaceText, state.copyText, actions)
}

@Composable
private fun ReadAsCard(readAs: String, ignoredWords: List<String>) {
    Card {
        Text(
            stringResource(R.string.selection_read_as),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(readAs, style = MaterialTheme.typography.displaySmall)
        if (ignoredWords.isNotEmpty()) {
            Text(
                stringResource(R.string.selection_ignored_words, ignoredWords.joinToString(", ")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Replace is primary when the field is editable; otherwise Copy is primary and a caption says why. */
@Composable
private fun ResultActions(replaceText: String?, copyText: String, actions: SelectionActions, replaceLabel: String? = null) {
    if (replaceText != null) {
        PillButton(replaceLabel ?: stringResource(R.string.selection_replace_with_result), primary = true) { actions.onReplace(replaceText) }
        PillButton(stringResource(R.string.selection_copy), primary = false) { actions.onCopy(copyText) }
    } else {
        PillButton(stringResource(R.string.selection_copy), primary = true) { actions.onCopy(copyText) }
        Text(
            stringResource(R.string.selection_read_only_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AmbiguousBody(state: SelectionUiState.Ambiguous, actions: SelectionActions) {
    val colors = LocalHisaabColors.current
    Row(
        Modifier.fillMaxWidth().background(colors.cautionContainer, RoundedCornerShape(16.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = colors.cautionIcon)
        Column {
            Text(stringResource(R.string.selection_ambiguous_title), style = MaterialTheme.typography.bodyLarge, color = colors.onCautionContainer)
            Text(stringResource(R.string.selection_ambiguous_body), style = MaterialTheme.typography.bodyMedium, color = colors.onCautionContainer)
        }
    }
    Text(stringResource(R.string.selection_ambiguous_question), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Column(Modifier.selectableGroup()) {
        state.readings.forEachIndexed { index, reading ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 64.dp)
                    .selectable(selected = index == state.selectedIndex, role = Role.RadioButton, onClick = { actions.onSelectReading(index) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = index == state.selectedIndex, onClick = null)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(reading.readAs, style = MaterialTheme.typography.bodyLarge)
                    Text(reading.resultText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    val chosen = state.selected
    ResultActions(
        replaceText = chosen.replaceText,
        copyText = chosen.copyText,
        actions = actions,
        replaceLabel = chosen.replaceText?.let { stringResource(R.string.selection_replace_with, it) },
    )
}

@Composable
private fun NothingBody(state: SelectionUiState.NothingToCalculate, actions: SelectionActions) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.selection_nothing_title), style = MaterialTheme.typography.titleLarge)
    }
    Text(stringResource(R.string.selection_nothing_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Card { Text(stringResource(R.string.selection_you_selected, state.selectedText), style = MaterialTheme.typography.bodyMedium) }
    PillButton(stringResource(R.string.selection_open_app), primary = true) { actions.onOpenApp() }
    TextButton(onClick = actions.onClose, modifier = Modifier.fillMaxWidth().heightIn(min = PillHeight)) {
        Text(stringResource(R.string.selection_close))
    }
}

@Composable
private fun ErrorBody(state: SelectionUiState.Error, actions: SelectionActions) {
    val (reason, hint) = when (state.reason) {
        ErrorReason.DIVIDE_BY_ZERO -> R.string.error_divide_by_zero to R.string.error_divide_by_zero_hint
        ErrorReason.UNREADABLE -> R.string.error_unreadable to R.string.error_unreadable_hint
    }
    Card { Text(stringResource(R.string.selection_you_selected, state.selectedText), style = MaterialTheme.typography.bodyMedium) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(stringResource(reason), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
    }
    Text(stringResource(hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    TextButton(onClick = actions.onClose, modifier = Modifier.fillMaxWidth().heightIn(min = PillHeight)) {
        Text(stringResource(R.string.selection_close))
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) { content() }
}

@Composable
private fun PillButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().heightIn(min = PillHeight)
    if (primary) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}
