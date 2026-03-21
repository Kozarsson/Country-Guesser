package org.kth.countryguesser.model.service

import org.kth.countryguesser.BuildConfig
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import java.math.BigDecimal
import java.math.BigInteger
import okhttp3.logging.HttpLoggingInterceptor

/** Defines an [ApiService] component using
 * [Square's](https://github.com/square) libraries:
 * * [OkHttp](https://github.com/square/okhttp) - [OkHttpClient]
 * * [Moshi](https://github.com/square/moshi) - [Moshi]
 * * [Retrofit](https://github.com/square/retrofit) - [API] is a generic interface that uses
 *   retrofit annotations.
 *
 * @param API A Retrofit interface.
 */
abstract class RetrofitApiService<out API> : ApiService<API, Moshi, OkHttpClient> {
    /** Adapter to deal with decimals.
     * Data classes should use [BigDecimal] when defining attributes containing decimals used
     * for calculations (like financial operations).
     * Float/Double may be used for simpler cases.
     */
    private object BigDecimalAdapter {
        @FromJson
        fun fromJson(value: String) = BigDecimal(value)

        @ToJson
        fun toJson(value: BigDecimal) = value.toString()
    }

    /** Adapter to deal with integers.
     * Data classes should use [BigInteger] when defining attributes containing integers used
     * for calculations (like financial operations).
     * Int/Long may be used for in most cases, except when having operations with [BigDecimal]s.
     */
    private object BigIntegerAdapter {
        @FromJson
        fun fromJson(value: String) = BigInteger(value)

        @ToJson
        fun toJson(value: BigInteger) = value.toString()
    }
    /** All parsing requirements (encode/decode) are processed by [Moshi].
     * One instance is enough - as per its documentation:
     * Moshi instances are thread-safe, meaning multiple threads can safely use a single instance concurrently.
     *
     * Adds adapters to handle:
     * * Integer values as [BigInteger]
     * * Decimal values as [BigDecimal]
     * * Date/time values as [java.time] - see [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt).
     */
    override val parser: Moshi by lazy {
        Moshi.Builder()
            .add(BigIntegerAdapter)
            .add(BigDecimalAdapter)
            .add(java.time.ZonedDateTime::class.java, Rfc3339DateJsonAdapter())
            .build()
    }

    /** [network] goes through [OkHttpClient]. */
    override val network: OkHttpClient by lazy {
        OkHttpClient.Builder()
            /* Adding required headers
            .addNetworkInterceptor { chain ->
                chain.proceed(
                    chain.request().let { req ->
                        req.headers.newBuilder().apply {
                            // add any custom header
                        }.build().let {
                            req.newBuilder().headers(it).build()
                        }
                    }
                )
            }
             */
            .also { builder ->
                if (BuildConfig.DEBUG) {
                    builder.addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }
}