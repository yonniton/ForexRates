package me.yonniton.forex.data

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Data-model for a
 * [forex-rates JSON response](https://revolut.duckdns.org/latest?base=EUR).
 */
class ForexRates(

    @SerializedName("base")
    val base: CurrencyCode,

    @SerializedName("date")
    val date: Date,

    @SerializedName("rates")
    val rates: Map<CurrencyCode, Double>
)
