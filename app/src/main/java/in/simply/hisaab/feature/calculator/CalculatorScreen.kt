package `in`.simply.hisaab.feature.calculator

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalContext
import `in`.simply.hisaab.core.designsystem.HisaabDimens
import `in`.simply.hisaab.feature.history.HistoryActions
import `in`.simply.hisaab.feature.history.HistoryGrouping
import `in`.simply.hisaab.feature.history.HistoryRecorder
import `in`.simply.hisaab.feature.history.HistoryScreen
import java.util.TimeZone
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.simply.hisaab.R
import `in`.simply.hisaab.core.designsystem.LocalHisaabColors
import `in`.simply.hisaab.core.designsystem.LocalHisaabTypeScale
import `in`.simply.hisaab.core.designsystem.toTextStyle
import `in`.simply.hisaab.feature.history.HistoryRepository
import `in`.simply.hisaab.parser.ErrorReason

/**
 * Stateful calculator: owns the expression, wires the keypad to the reducer, saves committed sums
 * to [history], and switches to the History screen.
 */
@Composable
fun CalculatorRoute(history: HistoryRepository, modifier: Modifier = Modifier) {
    var expression by rememberSaveable { mutableStateOf("") }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    if (showHistory) {
        BackHandler { showHistory = false }
        val entries by history.entries.collectAsState()
        val sections = remember(entries) { HistoryGrouping.group(entries, System.currentTimeMillis(), TimeZone.getDefault()) }
        HistoryScreen(
            sections = sections,
            actions = HistoryActions(
                onBack = { showHistory = false },
                // Tap reuses the result: it becomes the expression, with the real minus sign.
                onReuse = { entry -> expression = entry.value.replace('-', '−'); showHistory = false },
                onCopy = { entry ->
                    context.getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("Hisaab result", entry.value))
                },
                onDelete = { entry -> history.delete(entry.id) },
                onClearAll = { history.clear() },
            ),
            modifier = modifier,
        )
    } else {
        CalculatorScreen(
            expression = expression,
            onKey = { key ->
                if (key == CalculatorKey.EQUALS) {
                    HistoryRecorder.fromCalculator(expression, System.currentTimeMillis())?.let(history::add)
                }
                expression = CalculatorReducer.press(expression, key)
            },
            onOpenHistory = { showHistory = true },
            modifier = modifier,
        )
    }
}

@Composable
fun CalculatorScreen(
    expression: String,
    onKey: (CalculatorKey) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val display = remember(expression) { CalculatorDisplay.from(expression) }
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.app_brand),
                style = LocalHisaabTypeScale.current.title.toTextStyle(),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenHistory, modifier = Modifier.size(HisaabDimens.MinTouchTargetDp.dp)) {
                Icon(HistoryIcon, contentDescription = stringResource(R.string.history_title))
            }
        }
        DisplayZone(display, Modifier.weight(1f))
        Keypad(display, onKey)
    }
}

/** The clock-with-arrow from the board, drawn from its stroke paths so it follows the theme colour. */
private val HistoryIcon: ImageVector = ImageVector.Builder(
    name = "History", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
).apply {
    listOf("M3 12a9 9 0 109-9", "M3 4v5h5", "M12 7v5l3 2").forEach { path ->
        addPath(
            pathData = addPathNodes(path),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
}.build()

@Composable
private fun DisplayZone(display: CalculatorDisplay, modifier: Modifier = Modifier) {
    val scale = LocalHisaabTypeScale.current
    Column(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.End,
    ) {
        if (display.isEmpty) {
            Text(
                text = stringResource(R.string.calculator_placeholder),
                style = scale.numberM.toTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = display.expressionText,
                style = scale.numberM.toTextStyle(),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().testTag("expression"),
            )
        }
        if (display.error != null) {
            ErrorLine(display.error)
        } else {
            BasicText(
                text = display.resultText,
                style = scale.numberXl.toTextStyle().copy(
                    color = if (display.isEmpty) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                maxLines = 1,
                // The result shrinks to 32 sp before it ever wraps or is cut.
                autoSize = TextAutoSize.StepBased(minFontSize = 32.sp, maxFontSize = scale.numberXl.sizeSp.sp),
                modifier = Modifier.fillMaxWidth().testTag("result").semantics { liveRegion = LiveRegionMode.Polite },
            )
            Text(
                text = display.wordLine.orEmpty(),
                style = scale.body.toTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.heightIn(min = 24.dp).testTag("wordLine"),
            )
        }
    }
}

@Composable
private fun ErrorLine(reason: ErrorReason) {
    val message = when (reason) {
        ErrorReason.DIVIDE_BY_ZERO -> R.string.error_divide_by_zero
        ErrorReason.UNREADABLE -> R.string.error_unreadable
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(
            text = stringResource(message),
            style = LocalHisaabTypeScale.current.bodyL.toTextStyle().copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun Keypad(display: CalculatorDisplay, onKey: (CalculatorKey) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CalculatorKey.entries.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    KeyButton(
                        key = key,
                        enabled = key != CalculatorKey.EQUALS || display.equalsEnabled,
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** Operators use primary-container, "=" uses primary, function keys are outlined: more than colour tells them apart. */
@Composable
private fun KeyButton(key: CalculatorKey, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val hisaab = LocalHisaabColors.current
    val (container, content, border) = when (key.kind) {
        CalculatorKey.Kind.DIGIT -> Triple(hisaab.keySurface, scheme.onSurface, null)
        CalculatorKey.Kind.OPERATOR -> Triple(scheme.primaryContainer, scheme.onPrimaryContainer, null)
        CalculatorKey.Kind.FUNCTION -> Triple(scheme.surfaceContainer, scheme.onSurface, BorderStroke(1.dp, scheme.outline))
        CalculatorKey.Kind.EQUALS -> Triple(scheme.primary, scheme.onPrimary, null)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        border = border,
        modifier = modifier.heightIn(min = 64.dp).widthIn(min = 72.dp).semantics { contentDescription = key.spoken },
    ) {
        Box(Modifier.heightIn(min = 64.dp), contentAlignment = Alignment.Center) {
            Text(
                text = key.label,
                style = LocalHisaabTypeScale.current.keyLabel.toTextStyle(),
                color = if (enabled) Color.Unspecified else content.copy(alpha = 0.38f),
            )
        }
    }
}
