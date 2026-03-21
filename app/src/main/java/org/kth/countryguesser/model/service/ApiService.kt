package org.kth.countryguesser.model.service
/** Defines all services required to process any API request and response:
 * * Network connectivity
 * * Building an HTTP request (headers, query-params, path-params, request-body)
 * * Processing an HTTP response
 * * Handle any network errors
 * * Handle any HTTP errors
 * * Parse payloads
 *
 * @param API API utility.
 * @param PARSER Parser to use.
 * @param IO Networking library to use.
 */
interface ApiService<out API, out PARSER, out IO> {
    /** API component. Should include endpoint definition. */
    abstract val api: API
    /** Parsing component. */
    abstract val parser: PARSER
    /** Networking component. */
    abstract val network: IO
}