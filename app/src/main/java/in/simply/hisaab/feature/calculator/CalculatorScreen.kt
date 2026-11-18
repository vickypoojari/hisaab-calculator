package `in`.simply.hisaab.feature.calculator

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
import `in`.simply.hisaab.parser.ErrorReason

/** Stateful calculator: owns the expression and wires the keypad to the reducer. */
@Composable
fun CalculatorRoute(modifier: Modifier = Modifier) {
    var expression by rememberSaveable { mutableStateOf("") }
    CalculatorScreen(
        expression = expression,
        onKey = { key -> expression = CalculatorReducer.press(expression, key) },
        modifier = modifier,
    )
}

@Composable
fun CalculatorScreen(expression: String, onKey: (CalculatorKey) -> Unit, modifier: Modifier = Modifier) {
    val display = remember(expression) { CalculatorDisplay.from(expression) }
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            Text(stringResource(R.string.app_brand), style = LocalHisaabTypeScale.current.title.toTextStyle())
        }
        DisplayZone(display, Modifier.weight(1f))
        Keypad(display, onKey)
    }
}

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
