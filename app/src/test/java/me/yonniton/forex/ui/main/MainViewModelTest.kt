package me.yonniton.forex.ui.main

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import me.yonniton.forex.data.CurrencyCode
import me.yonniton.forex.data.ForexRates
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MainViewModelTest {

    private lateinit var scheduler: TestScheduler
    private lateinit var mockEndpoint: ForexRates.Endpoint
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var viewmodel: MainViewModel

    private val ratesResponse: Single<ForexRates> = Single.fromCallable {
        ClassLoader.getSystemResourceAsStream("rates.json")
            .use { Gson().fromJson(it.reader(), ForexRates::class.java) }
    }

    @Before
    fun setUp() {
        scheduler = TestScheduler()
        RxJavaPlugins.setIoSchedulerHandler { scheduler }
        RxJavaPlugins.setComputationSchedulerHandler { scheduler }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            object : Scheduler() {
                override fun createWorker(): Scheduler.Worker {
                    return ExecutorScheduler.ExecutorWorker(Executor { it.run() })
                }
            }
        }

        viewmodel = MainViewModel(ApplicationProvider.getApplicationContext())
        mockEndpoint = mock<ForexRates.Endpoint> {
            on { getForexRates(any()) } doReturn ratesResponse.delay(1, TimeUnit.SECONDS)
        }.also { viewmodel.endpoint = it }
        lifecycleRegistry = LifecycleRegistry(mock())
            .also { it.addObserver(viewmodel) }
    }

    @Test
    fun `when MainViewModel is instantiated, then it should be in the loading state, and it should have no ForexRates`() {
        with(viewmodel) {
            assertThat("the progress-spinner should be shown", progressVisibility.get(), equalTo(VISIBLE))
            assertThat("the rates-list should be hidden", ratesVisibility.get(), equalTo(GONE))
            assertThat("there should be no rates-response", rates, nullValue())
            assertThat("there should be no rates-adapter", ratesAdapter.get(), nullValue())
        }
        verify(mockEndpoint, never()).getForexRates(any())
    }

    @Test
    fun `when MainViewModel resumes, then it should be in the loading state, and it should have started a ForexRates query`() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        scheduler.triggerActions()
        with(viewmodel) {
            assertThat("the progress-spinner should be shown", progressVisibility.get(), equalTo(VISIBLE))
            assertThat("the rates-list should be hidden", ratesVisibility.get(), equalTo(GONE))
            assertThat("there should be no rates-response", rates, nullValue())
            assertThat("there should be no rates-adapter", ratesAdapter.get(), nullValue())
        }
        verify(mockEndpoint, only()).getForexRates(viewmodel.baseCurrency)
    }

    @Test
    fun `given MainViewModel started a ForexRates query, when the query fails, then the rates-list should not be shown`() {
        mockEndpoint.stub {
            on { getForexRates(any()) } doReturn Single.timer(1, TimeUnit.SECONDS).map<ForexRates> { throw IOException() }
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        scheduler.triggerActions()
        with(viewmodel) {
            assertThat("the progress-spinner should be shown", progressVisibility.get(), equalTo(VISIBLE))
            assertThat("the rates-list should be hidden", ratesVisibility.get(), equalTo(GONE))
            assertThat("there should be no rates-response", rates, nullValue())
            assertThat("there should be no rates-adapter", ratesAdapter.get(), nullValue())
        }
        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        with(viewmodel) {
            assertThat("the progress-spinner should be hidden", progressVisibility.get(), equalTo(GONE))
            assertThat("the rates-list should be hidden", ratesVisibility.get(), equalTo(GONE))
            assertThat("there should be no rates-response", rates, nullValue())
            assertThat("there should be no rates-adapter", ratesAdapter.get(), nullValue())
        }
        verify(mockEndpoint, only()).getForexRates(viewmodel.baseCurrency)
    }

    @Test
    fun `given MainViewModel started a ForexRates query, when a response arrives late, then the rates-list should not be shown`() {
        mockEndpoint.stub {
            on { getForexRates(any()) } doReturn ratesResponse.delay(5, TimeUnit.SECONDS)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        scheduler.advanceTimeBy(4999, TimeUnit.MILLISECONDS)
        with(viewmodel) {
            assertThat("the progress-spinner should be shown", progressVisibility.get(), equalTo(VISIBLE))
            assertThat("the rates-list should be hidden", ratesVisibility.get(), equalTo(GONE))
            assertThat("there should be no rates-response", rates, nullValue())
            assertThat("there should be no rates-adapter", ratesAdapter.get(), nullValue())
        }
        scheduler.advanceTimeTo(5, TimeUnit.SECONDS)
        with(viewmodel) {
            assertThat("the progress-spinner should be hidden", progressVisibility.get(), equalTo(GONE))
            assertThat("the rates-list should be hidden", ratesVisibility.get(), equalTo(GONE))
            assertThat("there should be no rates-response", rates, nullValue())
            assertThat("there should be no rates-adapter", ratesAdapter.get(), nullValue())
        }
        verify(mockEndpoint, only()).getForexRates(viewmodel.baseCurrency)
    }

    @Test
    fun `given MainViewModel started a ForexRates query, when a response arrives on-time, then it should show the rates-list`() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        with(viewmodel) {
            assertThat("the progress-spinner should be hidden", progressVisibility.get(), equalTo(GONE))
            assertThat("the rates-list should be shown", ratesVisibility.get(), equalTo(VISIBLE))
            assertThat("there should be a rates-response", rates, notNullValue())
            assertThat("there should be a rates-adapter", ratesAdapter.get(), notNullValue())
        }
        verify(mockEndpoint, times(2)).getForexRates(viewmodel.baseCurrency)
    }

    @Test
    fun `given MainViewModel started a ForexRates query, when several responses arrive on-time, then each response should update the rates-list`() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val ratesCollection = mutableSetOf<ForexRates>()
        repeat(3) {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
            viewmodel.rates?.also { ratesCollection.add(it) }
        }
        assertThat("there should be 3 rates-responses", ratesCollection, hasSize(3))
        verify(mockEndpoint, times(4)).getForexRates(viewmodel.baseCurrency)
    }

    @Test
    fun `given MainViewModel started a ForexRates query, when a new base currency is assigned, then the next query should use the new base currency`() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        // default EUR query fired
        scheduler.triggerActions()
        verify(mockEndpoint).getForexRates(CurrencyCode.EUR)

        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        // AUD assigned
        viewmodel.baseCurrency = CurrencyCode.AUD
        // AUD query not fired yet
        verify(mockEndpoint).getForexRates(CurrencyCode.EUR)

        // AUD query fired
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        verify(mockEndpoint).getForexRates(CurrencyCode.AUD)

        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        // GBP assigned
        viewmodel.baseCurrency = CurrencyCode.GBP
        // GBP query not fired yet
        verify(mockEndpoint).getForexRates(CurrencyCode.AUD)

        // GBP query fired
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        verify(mockEndpoint).getForexRates(CurrencyCode.GBP)

        // GBP query fired again
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        verify(mockEndpoint, times(2)).getForexRates(CurrencyCode.GBP)
    }
}
