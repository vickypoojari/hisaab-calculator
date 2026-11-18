package `in`.simply.hisaab.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeScaleTest {

    @Test
    fun `standard scale matches the design board`() {
        val s = HisaabTypeScale.Standard
        assertEquals(56, s.numberXl.sizeSp)
        assertEquals(40, s.numberL.sizeSp)
        assertEquals(28, s.numberM.sizeSp)
        assertEquals(22, s.title.sizeSp)
        assertEquals(12, s.caption.sizeSp)
    }

    @Test
    fun `large text scale is reserved with the sizes from the spec`() {
        val s = HisaabTypeScale.LargeText
        assertEquals(72, s.numberXl.sizeSp)
        assertEquals(36, s.keyLabel.sizeSp)
    }

    @Test
    fun `nothing in either scale drops below the 12sp floor and line height stays readable`() {
        listOf(HisaabTypeScale.Standard, HisaabTypeScale.LargeText).forEach { scale ->
            scale.all.forEach { style ->
                assertTrue(style.sizeSp >= 12, "${style.name} is ${style.sizeSp}sp")
                assertTrue(style.lineHeightSp > style.sizeSp, "${style.name} line height must exceed size")
            }
        }
    }

    @Test
    fun `touch targets and spacing follow the 8dp rhythm`() {
        assertEquals(48, HisaabDimens.MinTouchTargetDp)
        assertEquals(listOf(4, 8, 12, 16, 24, 32, 48), HisaabDimens.Spacing)
    }
}
