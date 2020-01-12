package me.yonniton.forex.ui.main

import android.app.Application
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.databinding.ObservableDouble
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.yonniton.forex.data.CurrencyCode
import me.yonniton.forex.data.ForexRates
import me.yonniton.forex.ui.CurrenciesAdapter
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : LifecycleObserver, AndroidViewModel(application) {

    val progressVisibility = ObservableField<Int>(VISIBLE)
    val ratesVisibility = ObservableField<Int>(GONE)
    val ratesAdapter = ObservableField<CurrenciesAdapter>()
    val baseAmount = ObservableDouble(1.0)

    internal var rates: ForexRates? = null

    internal var endpoint: ForexRates.Endpoint = ForexRates.ENDPOINT

    var baseCurrency: CurrencyCode = CurrencyCode.EUR

    private var disposable: Disposable? = null
        set(value) {
            field?.dispose()
            field = value
        }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun queryRates() {
        endpoint.getForexRates(baseCurrency)
            .delay(1, TimeUnit.SECONDS)
            .timeout(5, TimeUnit.SECONDS)
            .repeat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                // request success, populate forex-rates list
                { ratesResponse ->
                    progressVisibility.set(GONE)
                    ratesVisibility.set(VISIBLE)
                    ratesAdapter.get()
                        ?.apply {
                            viewModel.rates = ratesResponse
                            notifyItemRangeChanged(1, itemCount - 1)
                        }
                        ?: run { ratesAdapter.set(CurrenciesAdapter(this@MainViewModel)) }
                },
                // request failure, blank screen with error Toast
                {
                    Toast.makeText(getApplication(), "could not retrieve forex rates", Toast.LENGTH_SHORT).show()
                    progressVisibility.set(GONE)
                    ratesVisibility.set(GONE)
                }
            )
            .also {// request fired, show loading UI
                disposable = it
                progressVisibility.set(VISIBLE)
                ratesVisibility.set(GONE)
            }
    }

    override fun onCleared() {
        disposable = null
        super.onCleared()
    }
}
