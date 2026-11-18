package `in`.simply.hisaab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import `in`.simply.hisaab.feature.calculator.CalculatorRoute
import `in`.simply.hisaab.feature.history.HistoryRepository
import `in`.simply.hisaab.feature.onboarding.HintChip
import `in`.simply.hisaab.feature.onboarding.OnboardingProgress
import `in`.simply.hisaab.feature.onboarding.OnboardingScreen

/**
 * Chooses between onboarding and the calculator. Onboarding opens on the first launch, and again
 * from the hint chip or the More menu. The calculator keeps what was typed while it is away.
 */
@Composable
fun AppRoot(history: HistoryRepository, progress: OnboardingProgress, hintChip: HintChip, modifier: Modifier = Modifier) {
    var showOnboarding by rememberSaveable { mutableStateOf(!progress.isDone) }
    val calculatorState = rememberSaveableStateHolder()
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = {
                progress.markDone()
                showOnboarding = false
            },
            modifier = modifier,
        )
    } else {
        calculatorState.SaveableStateProvider("calculator") {
            CalculatorRoute(
                history = history,
                modifier = modifier,
                hintChip = hintChip,
                onShowHowTo = { showOnboarding = true },
            )
        }
    }
}
