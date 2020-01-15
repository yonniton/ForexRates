package me.yonniton.forex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import me.yonniton.forex.R
import me.yonniton.forex.data.CurrencyCode
import me.yonniton.forex.databinding.ForexRatesListItemBinding
import me.yonniton.forex.ui.main.MainViewModel

class CurrenciesAdapter(internal val viewModel: MainViewModel) : RecyclerView.Adapter<CurrenciesAdapter.CurrencyItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemHolder {
        return DataBindingUtil.inflate<ForexRatesListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.forex_rates_list_item,
            parent,
            false
        ).let { binding -> CurrencyItemHolder(binding) }
    }

    override fun onBindViewHolder(holder: CurrencyItemHolder, position: Int) {
        val rates = viewModel.rates ?: throw IllegalStateException("missing forex rates")
        val currency: CurrencyCode
        // item[0] in the adapter shall be the base currency
        if (position < 1) {
            currency = rates.base
            holder.binding.currencyAmount.apply {
                isEnabled = true
                setText("%.2f".format(1.0))
            }
        }
        // item[position > 0] in the adapter shall be a quote currency
        else {
            currency = rates.rates.keys.elementAt(position - 1)
            val quoteAmount: Double = rates.rates.getOrElse(
                currency,
                { throw IllegalStateException("missing rate for currency[$currency]") }
            ) * viewModel.baseAmount.get()
            holder.binding.currencyAmount.setText("%.2f".format(quoteAmount))
            holder.binding.currencyFlag.setOnClickListener {
                viewModel.baseCurrency = currency
            }
        }

        with(holder.binding) {
            currencyFlag.setImageResource(currency.displayIcon)
            currencyCode.text = currency.name
            currencyName.text = currency.displayName
        }
    }

    override fun getItemCount(): Int {
        return viewModel.rates?.let {
            it.rates.entries.size + 1  // base currency + the set of quote currencies
        } ?: 0
    }

    class CurrencyItemHolder(internal val binding: ForexRatesListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
