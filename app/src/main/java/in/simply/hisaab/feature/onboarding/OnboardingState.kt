package `in`.simply.hisaab.feature.onboarding

/** Whether the two first-launch screens have been seen (finished or skipped). */
class OnboardingProgress(private val prefs: Preferences) {
    val isDone: Boolean get() = prefs.getBoolean(DONE, false)

    fun markDone() = prefs.putBoolean(DONE, true)

    private companion object {
        const val DONE = "onboarding_done"
    }
}

/**
 * The "Calculate from any app: see how" chip on the empty calculator: at most [maxShowings]
 * launches, and never again once dismissed or used. One instance per launch: the first question
 * uses up a showing, later questions in the same launch repeat the same answer.
 */
class HintChip(private val prefs: Preferences, private val maxShowings: Int = 3) {
    private var decidedThisLaunch: Boolean? = null

    fun isVisibleThisLaunch(): Boolean {
        if (prefs.getBoolean(DISMISSED, false)) return false
        decidedThisLaunch?.let { return it }
        val shown = prefs.getInt(SHOWN, 0)
        val visible = shown < maxShowings
        if (visible) prefs.putInt(SHOWN, shown + 1)
        decidedThisLaunch = visible
        return visible
    }

    fun dismiss() = prefs.putBoolean(DISMISSED, true)

    /** Opening the how-to from the chip counts as having seen it. */
    fun opened() = dismiss()

    private companion object {
        const val DISMISSED = "hint_chip_dismissed"
        const val SHOWN = "hint_chip_showings"
    }
}
