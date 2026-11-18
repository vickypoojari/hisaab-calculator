package `in`.simply.hisaab.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.simply.hisaab.R
import `in`.simply.hisaab.core.designsystem.HisaabDimens
import `in`.simply.hisaab.core.designsystem.LocalHisaabColors
import `in`.simply.hisaab.core.designsystem.LocalHisaabTypeScale
import `in`.simply.hisaab.core.designsystem.toTextStyle

private val PillHeight = 56.dp
private val MinTarget = HisaabDimens.MinTouchTargetDp.dp

/**
 * Two first-launch screens: where "Calculate" hides in the selection toolbar, then a real practice
 * line. [onFinish] is called by Skip on either screen and by Done.
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = MinTarget), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.onboarding_step, step),
                style = LocalHisaabTypeScale.current.label.toTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onFinish, modifier = Modifier.heightIn(min = MinTarget)) {
                Text(stringResource(R.string.onboarding_skip), style = LocalHisaabTypeScale.current.body.toTextStyle())
            }
        }
        // Only the explanation scrolls. With big text or a short screen the buttons below must stay
        // in reach, so they are never part of the scrolling area.
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (step == 1) StepOne() else StepTwo()
        }
        PageDots(current = step)
        if (step == 1) {
            PillButton(stringResource(R.string.onboarding_next)) { step = 2 }
        } else {
            PillButton(stringResource(R.string.onboarding_done), onClick = onFinish)
            TextButton(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth().heightIn(min = PillHeight)) {
                Text(stringResource(R.string.onboarding_back), style = LocalHisaabTypeScale.current.body.toTextStyle())
            }
        }
    }
}

@Composable
private fun StepOne() {
    Illustration()
    Heading(stringResource(R.string.onboarding_step1_title))
    BodyText(stringResource(R.string.onboarding_step1_body))
}

@Composable
private fun StepTwo() {
    Heading(stringResource(R.string.onboarding_step2_title))
    BodyText(stringResource(R.string.onboarding_step2_body))
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(24.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.onboarding_practice_label),
            style = LocalHisaabTypeScale.current.label.toTextStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // A real selectable line: the system toolbar and its Calculate action appear on it.
        SelectionContainer {
            Text(
                stringResource(R.string.onboarding_practice_text),
                style = LocalHisaabTypeScale.current.title.toTextStyle().copy(lineHeight = 32.sp),
            )
        }
    }
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(
            stringResource(R.string.onboarding_step2_info),
            style = LocalHisaabTypeScale.current.body.toTextStyle(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = LocalHisaabTypeScale.current.numberM.toTextStyle().copy(fontWeight = FontWeight.Medium),
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun BodyText(text: String) {
    Text(text, style = LocalHisaabTypeScale.current.bodyL.toTextStyle(), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Drawn, not a screenshot, so it follows the theme. Described once for TalkBack; the parts stay silent. */
@Composable
private fun Illustration() {
    val scheme = MaterialTheme.colorScheme
    val description = stringResource(R.string.onboarding_illustration)
    Column(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainer, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            buildAnnotatedString {
                append(stringResource(R.string.onboarding_demo_lead))
                withStyle(SpanStyle(background = scheme.primaryContainer, color = scheme.onPrimaryContainer)) {
                    append(stringResource(R.string.onboarding_demo_selected))
                }
            },
            style = LocalHisaabTypeScale.current.bodyL.toTextStyle(),
        )
        Row(
            Modifier.shadow(4.dp, RoundedCornerShape(28.dp), clip = false).background(LocalHisaabColors.current.keySurface, RoundedCornerShape(28.dp)).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(R.string.onboarding_demo_cut, R.string.onboarding_demo_copy, R.string.onboarding_demo_paste).forEach {
                ToolbarItem(stringResource(it))
            }
            Box(Modifier.ringed(RoundedCornerShape(24.dp)).size(MinTarget), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
                Badge("1", Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-14).dp))
            }
        }
        Column(
            Modifier
                .align(Alignment.End)
                .width(216.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
                .background(LocalHisaabColors.current.keySurface, RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp),
        ) {
            MenuItem(stringResource(R.string.onboarding_demo_share), highlighted = false)
            MenuItem(stringResource(R.string.onboarding_demo_select_all), highlighted = false)
            MenuItem(stringResource(R.string.selection_action_label), highlighted = true)
        }
    }
}

@Composable
private fun ToolbarItem(label: String) {
    Box(Modifier.defaultMinSize(minWidth = MinTarget, minHeight = MinTarget).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, style = LocalHisaabTypeScale.current.label.toTextStyle(), maxLines = 1, softWrap = false)
    }
}

@Composable
private fun MenuItem(label: String, highlighted: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MinTarget)
            .padding(horizontal = if (highlighted) 4.dp else 0.dp)
            .then(if (highlighted) Modifier.background(scheme.primaryContainer, RoundedCornerShape(12.dp)).ringed(RoundedCornerShape(12.dp)) else Modifier)
            .padding(horizontal = if (highlighted) 12.dp else 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            label,
            style = LocalHisaabTypeScale.current.body.toTextStyle().copy(fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal),
            color = if (highlighted) scheme.onPrimaryContainer else scheme.onSurface,
        )
        if (highlighted) Badge("2", Modifier.align(Alignment.CenterStart).offset(x = (-24).dp))
    }
}

@Composable
private fun Modifier.ringed(shape: RoundedCornerShape): Modifier = this.border(3.dp, MaterialTheme.colorScheme.primary, shape)

@Composable
private fun Badge(number: String, modifier: Modifier = Modifier) {
    Box(
        modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            number,
            style = LocalHisaabTypeScale.current.label.toTextStyle().copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/** Decorative: the "Step n of 2" text already says where you are. */
@Composable
private fun PageDots(current: Int) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 16.dp).clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..2).forEach { dot ->
            val active = dot == current
            Box(
                Modifier
                    .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Composable
private fun PillButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = PillHeight)) {
        Text(label, style = LocalHisaabTypeScale.current.body.toTextStyle().copy(fontWeight = FontWeight.Medium))
    }
}
