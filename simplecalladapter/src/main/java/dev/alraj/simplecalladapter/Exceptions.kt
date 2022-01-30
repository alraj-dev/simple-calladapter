package dev.alraj.simplecalladapter

import retrofit2.Response
/**
 * When [Response] is not successful [Response.isSuccessful].
 */
class FailedResponseException(message: String) : Exception(message) {
    constructor() : this("Retrofit response is not successful")
}

/**
 * When [Response.body] is null.
 */
class NullDataException(message: String) : Exception(message) {
    constructor() : this("Retrofit response is null")
}

/**
 * When [Response.body] is a [Collection] or [Array] and is empty.
 */
class EmptyListException(message: String) : Exception(message) {
    constructor() : this("Retrofit response body is an empty array or collection")
}