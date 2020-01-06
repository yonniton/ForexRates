package me.yonniton.forex.data

import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test

class CurrencyCodeTest {

    @Test
    fun `there should be 33 supported currency-codes`() {
        val enums = CurrencyCode.values()
        assertThat("CurrencyCode should support 33 currency-codes", enums, arrayWithSize(33))
    }

    @Test
    fun `there should be a currency-code string for each CurrencyCode`() {
        val codes = CurrencyCode.values()
            .map { it.name }
            .filter { "[A-Z]{3}".toRegex().matches(it) }
        assertThat("every CurrencyCode should have an associated 3-char currency-code", codes, hasSize(33))
    }

    @Test
    fun `there should be a displayName for each CurrencyCode`() {
        val displayNames = CurrencyCode.values()
            .map { it.displayName }
            .filter { it.isNotEmpty() }
        assertThat("every CurrencyCode should have a displayName", displayNames, hasSize(33))
    }
}
