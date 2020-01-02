### Forex Rates

![ForexRates](https://user-images.githubusercontent.com/26304959/73606577-ad450e00-45e6-11ea-8754-9d4bdc543959.gif)

A Forex-themed Android experiment:
- fetches fresh forex-rates from a web-endpoint at regular intervals
- refreshes the display of the forex-rates on a list-view
- allows selecting a base-currency
- allows specifying an amount of the base-currency;
  from which, the equivalent displayed quote-currency amounts are updated 

Leverages several popular frameworks:

- REST API consumption with [Retrofit](https://square.github.io/retrofit/)
- asynchronous I/O with [RxJava](https://github.com/ReactiveX/RxJava/tree/2.x)
- serialisation / deserialisation with [Gson](https://github.com/google/gson)
- displaying a collection of data with [Android's `RecyclerView`](https://developer.android.com/guide/topics/ui/layout/recyclerview)
- unit-testing with [Mockito](https://github.com/nhaarman/mockito-kotlin)
