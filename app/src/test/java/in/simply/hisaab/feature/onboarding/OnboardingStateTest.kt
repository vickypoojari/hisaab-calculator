package `in`.simply.hisaab.feature.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStateTest {

    private class FakePreferences : Preferences {
        private val values = mutableMapOf<String, Any>()
        override fun getBoolean(key: String, default: Boolean) = values[key] as? Boolean ?: default
        override fun putBoolean(key: String, value: Boolean) { values[key] = value }
        override fun getInt(key: String, default: Int) = values[key] as? Int ?: default
        override fun putInt(key: String, value: Int) { values[key] = value }
    }

    private val prefs = FakePreferences()

    // --- first launch ---

    @Test
    fun `a fresh install has not seen onboarding`() {
        assertFalse(OnboardingProgress(prefs).isDone)
    }

    @Test
    fun `finishing or skipping is remembered across launches`() {
        OnboardingProgress(prefs).markDone()

        assertTrue(OnboardingProgress(prefs).isDone)
    }

    // --- hint chip ---

    /** One call per app launch, the way the app asks. */
    private fun launchShowsChip(max: Int = 3) = HintChip(prefs, max).isVisibleThisLaunch()

    @Test
    fun `the chip shows on the first three launches and then stops`() {
        assertTrue(launchShowsChip())
        assertTrue(launchShowsChip())
        assertTrue(launchShowsChip())
        assertFalse(launchShowsChip())
        assertFalse(launchShowsChip())
    }

    @Test
    fun `asking again in the same launch does not use up another showing`() {
        val chip = HintChip(prefs)

        repeat(10) { assertTrue(chip.isVisibleThisLaunch()) }

        assertTrue(launchShowsChip())
        assertTrue(launchShowsChip())
        assertFalse(launchShowsChip())
    }

    @Test
    fun `dismissing hides it now and on every later launch`() {
        val chip = HintChip(prefs)
        assertTrue(chip.isVisibleThisLaunch())

        chip.dismiss()

        assertFalse(chip.isVisibleThisLaunch())
        assertFalse(launchShowsChip())
    }

    @Test
    fun `opening the how-to from the chip counts as seen`() {
        val chip = HintChip(prefs)
        assertTrue(chip.isVisibleThisLaunch())

        chip.opened()

        assertFalse(chip.isVisibleThisLaunch())
        assertFalse(launchShowsChip())
    }

    @Test
    fun `the number of showings can be changed`() {
        assertTrue(launchShowsChip(max = 1))
        assertFalse(launchShowsChip(max = 1))
    }
}
