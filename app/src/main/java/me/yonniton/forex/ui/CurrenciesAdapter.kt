package me.yonniton.forex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
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

        internal const val CURRENCY = 1
        internal const val AMOUNT = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CurrencyItem>() {
            override fun areItemsTheSame(oldItem: CurrencyItem, newItem: CurrencyItem): Boolean {
                return oldItem.currency == newItem.currency
            }

            override fun areContentsTheSame(oldItem: CurrencyItem, newItem: CurrencyItem): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: CurrencyItem, newItem: CurrencyItem): Any? {
                return when {
                    oldItem.currency != newItem.currency -> CURRENCY
                    oldItem.amount != newItem.amount -> AMOUNT
                    else -> null
                }
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
        val currencyItem = getItem(position)
        // item[0] in the adapter shall be the base currency
        if (position < 1) {
            holder.quoteAmount.set("%.2f".format(viewModel.baseAmount.get()))
            holder.binding.currencyAmount.apply {
                isEnabled = true
                doOnTextChanged { text, _, _, _ ->
                    text?.toString()
                        ?.toDoubleOrNull()
                        ?.also { viewModel.baseAmount.set(it) }
                }
            }
            holder.setIsRecyclable(false)
        }
        // item[position > 0] in the adapter shall be a quote currency
        else {
            val quoteAmount: Double = currencyItem.amount * viewModel.baseAmount.get()
            holder.quoteAmount.set("%.2f".format(quoteAmount))
            holder.binding.currencyFlag.setOnClickListener {
                viewModel.baseCurrency = currencyItem.currency
            }
        }

        holder.apply {
            flag.set(currencyItem.currency.displayIcon)
            code.set(currencyItem.currency.name)
            name.set(currencyItem.currency.displayName)
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
