package org.kth.countryguesser.model.repository

import org.kth.countryguesser.model.service.ApiService

/** This class defines all the repository mechanisms.
 *
 * It defines:
 * * When to call a remote service.
 * * When to call a local service.
 * * How to evaluate and cache data.
 *
 */
abstract class ApiRepository<API, out S : ApiService<API, *, *>>(
    private val service: S
) {
    /** Shortcut to the actual [ApiService.api]. */
    val api: API get() = service.api
    // You should add more functions to simplify how to handle errors
    // Something like:
    suspend fun <T> callApi(block: suspend (API) -> T): T =
        try {
            block.invoke(api)
        } catch (e: Exception) {
            // TODO: handle specific exceptions from retrofit, moshi, okhttp, etc.
            throw e // Temporary: rethrow to keep compilation working
        }
}