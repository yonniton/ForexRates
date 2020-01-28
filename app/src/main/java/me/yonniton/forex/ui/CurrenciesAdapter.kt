package me.yonniton.forex.ui

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.yonniton.forex.R
import me.yonniton.forex.data.CurrencyCode
import me.yonniton.forex.databinding.ForexRatesListItemBinding
import me.yonniton.forex.ui.main.MainViewModel

class CurrenciesAdapter(internal val viewModel: MainViewModel) : ListAdapter<CurrenciesAdapter.CurrencyItem, CurrenciesAdapter.CurrencyItemHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CurrencyItem>() {
            override fun areItemsTheSame(oldItem: CurrencyItem, newItem: CurrencyItem): Boolean {
                return oldItem.currency == newItem.currency
            }

            override fun areContentsTheSame(oldItem: CurrencyItem, newItem: CurrencyItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemHolder {
        return DataBindingUtil.inflate<ForexRatesListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.forex_rates_list_item,
            parent,
            false
        )
            .let { binding ->
                val holder = CurrencyItemHolder(binding)
                binding.viewModel = holder
                holder
            }
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
            holder.quoteAmount.set("%.2f".format(quoteAmount))
            holder.binding.currencyFlag.setOnClickListener {
                viewModel.baseCurrency = currency
            }
        }

        holder.binding.viewModel
            ?.takeIf { !TextUtils.equals(it.code.get(), currency.name) }
            ?.apply {
                flag.set(currency.displayIcon)
                code.set(currency.name)
                name.set(currency.displayName)
            }
    }

    override fun getItemCount(): Int {
        return viewModel.rates?.let {
            it.rates.entries.size + 1  // base currency + the set of quote currencies
        } ?: 0
    }

    data class CurrencyItem(
        val currency: CurrencyCode,
        var amount: Double = 0.0
    )

    class CurrencyItemHolder(internal val binding: ForexRatesListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val flag = ObservableInt(android.R.drawable.ic_menu_myplaces)
        val code = ObservableField<CharSequence>("")
        val name = ObservableField<CharSequence>("")
        val quoteAmount = ObservableField<CharSequence>("%.2f".format(0.0))
    }
}
