package me.yonniton.forex.data

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Single
import org.hamcrest.Matchers.*
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

    @Test
    fun `given a ForexRates_Endpoint, when a request is subscribed to, then a ForexRates instance should be emitted`() {
        val endpoint = mock<ForexRates.Endpoint> {
            on { getForexRates(any()) } doReturn Single.just(rates)
        }

        with(endpoint.getForexRates(CurrencyCode.EUR).test()) {
            assertValueCount(1)
            assertComplete()
            assertValue { value -> value.base == CurrencyCode.EUR }
            assertValue { value -> "%tF".format(value.date) == "2018-09-06" }
            assertValue { value -> value.rates.size == 32 }
        }
    }

    @Test
    fun `given a ForexRates instance, when a quantity of base currency is converted to any supported quote currency, then a calculated amount of quote currency should be returned`() {
        val results = CurrencyCode.values()
            .map { quote -> rates.convert(1, quote) }
        assertThat("all supported currencies should be convertible", results.size, equalTo(CurrencyCode.values().size))
    }

    @Test
    fun `given a ForexRates instance, when a quantity of base currency is converted to a quote currency, then the calculated amount of quote currency should be correct`() {
        assertThat("EUR -100 should convert to AUD -162.07" , rates.convert(-100L, CurrencyCode.AUD), closeTo(-162.07, 1e-5))
        assertThat("EUR -0.1 should convert to CNY -0.79663", rates.convert(-0.1f, CurrencyCode.CNY), closeTo(-0.79663, 1e-5))
        assertThat("EUR  -50 should convert to EUR -50"     , rates.convert(  -50, CurrencyCode.EUR), equalTo(-50.0))
        assertThat("EUR  0.2 should convert to GBP 0.180128", rates.convert(  0.2, CurrencyCode.GBP), closeTo(0.180128, 1e-5))
        assertThat("EUR  100 should convert to JPY 12990.0" , rates.convert(  100, CurrencyCode.JPY), equalTo(12990.0))
        assertThat("EUR   20 should convert to NZD 35.36"   , rates.convert(  20L, CurrencyCode.NZD), closeTo(35.36, 1e-5))
        assertThat("EUR    0 should convert to USD 0.0"     , rates.convert(    0, CurrencyCode.USD), equalTo(0.0))
    }
}
