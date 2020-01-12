package me.yonniton.forex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import me.yonniton.forex.R
import me.yonniton.forex.data.CurrencyCode
import me.yonniton.forex.data.ForexRates
import me.yonniton.forex.databinding.ForexRatesListItemBinding

class CurrenciesAdapter(internal var rates: ForexRates) : RecyclerView.Adapter<CurrenciesAdapter.CurrencyItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemHolder {
        return DataBindingUtil.inflate<ForexRatesListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.forex_rates_list_item,
            parent,
            false
        ).let { binding -> CurrencyItemHolder(binding) }
    }

    override fun onBindViewHolder(holder: CurrencyItemHolder, position: Int) {
        val currency: CurrencyCode
        val rate: Double
        // item[0] in the adapter shall be the base currency
        if (position < 1) {
            currency = rates.base
            rate = 1.0
        }
        // item[position > 0] in the adapter shall be a quote currency
        else {
            currency = rates.rates.keys.elementAt(position - 1)
            rate = rates.rates.getOrElse(
                currency,
                { throw IllegalStateException("missing rate for currency[$currency]") }
            )
        }

        with(holder.binding) {
            currencyFlag.setImageResource(currency.displayIcon)
            currencyCode.text = currency.name
            currencyName.text = currency.displayName
            currencyAmount.setText("%.2f".format(rate))
        }
    }

    override fun getItemCount(): Int {
        return rates.rates.entries.size + 1 // base currency + the set of quote currencies
    }

    class CurrencyItemHolder(internal val binding: ForexRatesListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
