package `in`.simply.hisaab

import android.app.Application
import `in`.simply.hisaab.feature.history.HistoryRepository
import `in`.simply.hisaab.feature.history.SqliteHistoryStore
import `in`.simply.hisaab.feature.onboarding.HintChip
import `in`.simply.hisaab.feature.onboarding.OnboardingProgress
import `in`.simply.hisaab.feature.onboarding.SharedPreferencesStore

/** Owns the one history list shared by the calculator and the selection sheet (same process). */
class HisaabApp : Application() {
    val history: HistoryRepository by lazy { HistoryRepository(SqliteHistoryStore(this)) }

    private val preferences by lazy { SharedPreferencesStore(this) }
    val onboardingProgress: OnboardingProgress by lazy { OnboardingProgress(preferences) }

    /** One per launch: the first question this process uses up one of the chip's three showings. */
    val hintChip: HintChip by lazy { HintChip(preferences) }
}
