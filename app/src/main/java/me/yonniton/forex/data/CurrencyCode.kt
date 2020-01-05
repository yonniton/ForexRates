package me.yonniton.forex.data

import androidx.annotation.DrawableRes
import me.yonniton.forex.R
import java.util.Currency

/**
 * Represents the currencies supported by the [ForexRates] API.
 * Each enumeration has an associated [Currency].
 */
enum class CurrencyCode(rDisplayIcon: Int) {

    AUD(R.drawable.ic_flag_aud),
    BGN(R.drawable.ic_flag_bgn),
    BRL(R.drawable.ic_flag_brl),
    CAD(R.drawable.ic_flag_cad),
    CHF(R.drawable.ic_flag_chf),
    CNY(R.drawable.ic_flag_cny),
    CZK(R.drawable.ic_flag_czk),
    DKK(R.drawable.ic_flag_dkk),
    EUR(R.drawable.ic_flag_eur),
    GBP(R.drawable.ic_flag_gbp),
    HKD(R.drawable.ic_flag_hkd),
    HRK(R.drawable.ic_flag_hrk),
    HUF(R.drawable.ic_flag_huf),
    IDR(R.drawable.ic_flag_idr),
    ILS(R.drawable.ic_flag_ils),
    INR(R.drawable.ic_flag_inr),
    ISK(R.drawable.ic_flag_isk),
    JPY(R.drawable.ic_flag_jpy),
    KRW(R.drawable.ic_flag_krw),
    MXN(R.drawable.ic_flag_mxn),
    MYR(R.drawable.ic_flag_myr),
    NOK(R.drawable.ic_flag_nok),
    NZD(R.drawable.ic_flag_nzd),
    PHP(R.drawable.ic_flag_php),
    PLN(R.drawable.ic_flag_pln),
    RON(R.drawable.ic_flag_ron),
    RUB(R.drawable.ic_flag_rub),
    SEK(R.drawable.ic_flag_sek),
    SGD(R.drawable.ic_flag_sgd),
    THB(R.drawable.ic_flag_thb),
    TRY(R.drawable.ic_flag_try),
    USD(R.drawable.ic_flag_usd),
    ZAR(R.drawable.ic_flag_zar);

    val currency: Currency = Currency.getInstance(this.name)

    val displayName: String
        get() = currency.displayName

    @DrawableRes val displayIcon: Int = rDisplayIcon
}
