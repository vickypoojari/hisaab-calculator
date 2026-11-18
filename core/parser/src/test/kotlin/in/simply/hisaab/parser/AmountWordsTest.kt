package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The muted caption under a result: lakh below 1 crore, crore from there up, from 1 lakh.
 * The board's own examples show two decimals ("2.05 lakh"), so that is the limit; trailing zeros go.
 */
class AmountWordsTest {

    private fun words(value: String): String? = AmountWords.of(BigDecimal(value))

    @Test
    fun `nothing below one lakh`() {
        assertNull(words("0"))
        assertNull(words("99999"))
        assertNull(words("99999.99"))
    }

    @Test
    fun `lakh from one lakh up to one crore`() {
        assertEquals("1 lakh", words("100000"))
        assertEquals("2.05 lakh", words("205000"))
        assertEquals("3 lakh", words("300000"))
        assertEquals("4.27 lakh", words("427125"))
    }

    @Test
    fun `a value that rounds up to a hundred lakh becomes one crore`() {
        assertEquals("1 crore", words("9999999"))
    }

    @Test
    fun `crore from one crore up, big ones grouped Indian style`() {
        assertEquals("1 crore", words("10000000"))
        assertEquals("2.5 crore", words("25000000"))
        assertEquals("12.35 crore", words("123456789"))
        assertEquals("1,234.57 crore", words("12345678901"))
    }

    @Test
    fun `negative amounts keep a real minus in front`() {
        assertEquals("−2.05 lakh", words("-205000"))
    }
}
