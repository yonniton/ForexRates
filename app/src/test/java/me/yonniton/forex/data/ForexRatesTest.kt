package me.yonniton.forex.data

import com.google.gson.Gson
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class ForexRatesTest {

    private lateinit var rates: ForexRates

    @Before
    fun setUp() {
        rates = ClassLoader.getSystemResourceAsStream("rates.json")
            .use { Gson().fromJson(it.reader(), ForexRates::class.java) }
    }

    @Test
    fun `given a forex-rates JSON response, when deserialized with Gson, then a ForexRates instance should be created`() {
        assertThat("ForexRates.base should be EUR", rates.base, equalTo(CurrencyCode.EUR))
        assertThat("ForexRates.date should be 2018-09-06", "%tF".format(rates.date), equalTo("2018-09-06"))
        assertThat("ForexRates.rates should have 32 entries", rates.rates.entries, hasSize(32))
    }
}
