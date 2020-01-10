package me.yonniton.forex.data

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
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
) {

    companion object {

        private const val DOMAIN = "https://revolut.duckdns.org/"

        val ENDPOINT: Endpoint = Retrofit.Builder()
            .baseUrl(DOMAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(Endpoint::class.java)
    }

    interface Endpoint {
        @GET("latest")
        fun getForexRates(@Query("base") base: CurrencyCode): Single<ForexRates>
    }

    /** converts [baseAmount] quantity of currency [base] to some quantity of currency [quote] */
    fun convert(baseAmount: Number, quote: CurrencyCode): Double {
        return if (quote == base) {
            baseAmount.toDouble()
        } else {
            rates[quote]
                ?.let { rate -> baseAmount.toDouble() * rate }
                ?: throw IllegalStateException("missing rate for currency[$quote]")
        }
    }
}
