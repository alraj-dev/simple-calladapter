package dev.alraj.simplecalladapter

import android.os.Handler
import android.os.Looper
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * A simple Retrofit2 Callback implementation which gives both response and exception in a single callback.
 * This class uses [DefaultConditions] to create exception when a condition is met in server [Response],
 * which simplifies nested condition used on normal response [Callback].
 */
class SimpleCall<R>(
    initialCall: Call<R>,
    defaultConditions: Array<DefaultConditions>?
    ) {
    // using var for retry clone
    private var call = initialCall

    private var tempDefaultConditions: MutableList<DefaultConditions> = defaultConditions?.toMutableList() ?: mutableListOf()

    /**
     * which type of request used before, [enqueue] or [execute]
     */
    private var whichCall = -1
    private var retry = false
    private lateinit var previousCallback: SimpleCallback<R>
    /**
     * exclude a default condition for this callback to make exception.
     * @param condition [DefaultConditions] to exclude
     */
    fun exclude(condition: DefaultConditions): SimpleCall<R> {
        tempDefaultConditions.remove(condition)
        return this
    }

    /**
     * include a default condition for this callback to make exception.
     * @param condition [DefaultConditions] to include
     */
    fun include(condition: DefaultConditions): SimpleCall<R> {
        tempDefaultConditions.add(condition)
        return this
    }

    /**
     * Run this request Synchronously in the same thread. Uses [Call.execute]
     * @param callback a [SimpleCallback] implementation which gives data and exception
     */
    fun execute(callback: SimpleCallback<R>) {
        whichCall = EXECUTE
        if(!retry) previousCallback = callback
        // a wrapper lambda to run the user callback
        val wrapperHandler: (R?, Throwable?) -> Unit = { body: R?, exception: Throwable? ->
            // run the callback in main thread
            callback.onResult(body, exception, this)
        }

        try {
            // call and handle response
            val response = call.execute()
            handleResponse(response, wrapperHandler)

        } catch (t: IOException) {
            callback.onResult(null, t, this)
        }
        retry = false
    }

    /**
     * Run this request Asynchronously in the background thread. Uses [Call.enqueue]
     * @param callback [SimpleCallback] implementation which gives data and exception
     */
    fun enqueue(callback: SimpleCallback<R>) {
        whichCall = ENQUEUE
        if(!retry) previousCallback = callback

        // a wrapper lambda to run the user callback in main thread
        val wrapperHandler: (R?, Throwable?) -> Unit = { body: R?, exception: Throwable? ->
            // run the callback in main thread
            Handler(Looper.getMainLooper()).post {
                callback.onResult(body, exception, this)
            }
        }

        // retrofit callback
        call.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>?, r: Response<R>) {
                handleResponse(r, wrapperHandler)
            }

            override fun onFailure(call: Call<R>?, t: Throwable) {
                handleException(t, wrapperHandler)
            }
        })
        retry = false
    }

    private fun handleResponse(response: Response<R>, responseHandler: (R?, Throwable?) -> Unit) {
        // if request is success, the response will have a body
        if (response.isSuccessful) {
            handleBody(response, responseHandler)
        }
        // otherwise handle failure state
        else {
            handleFailure(response, responseHandler)
        }
    }

    private fun handleBody(response: Response<R>, responseHandler: (R?, Throwable?) -> Unit) {
        // Response body is not null
        if (response.body() != null) {
            handleData(response, responseHandler)
        }
        // Response body is null and Flagged to transfer to FailureCall
        else if (DefaultConditions.NULL_RESPONSE in tempDefaultConditions) {
            responseHandler(
                null,
                NullDataException("Null body in client response, ${response.raw()}")
            )
        }
        // Response body is null, still give it to user
        else {
            responseHandler(null, null)
        }
    }

    private fun handleData(response: Response<R>, responseHandler: (R?, Throwable?) -> Unit) {
        // Response body is empty and flagged
        if (isEmpty(response) && DefaultConditions.EMPTY_LIST in tempDefaultConditions) {
            responseHandler(null, EmptyListException("Response body is an empty collection ${response.raw()}"))
        }
        // Response body is a class data or non empty or empty and not flagged
        else {
            responseHandler(response.body(), null)
        }
    }

    private fun isEmpty(response: Response<R>): Boolean {
        val isEmptyCollection = (response.body() is Collection<*> && (response.body() as Collection<*>).isEmpty())
        val isEmptyArray = (response.body() is Array<*> && (response.body() as Array<*>).isEmpty())
        val isEmptyMap = (response.body() is Map<*, *> && (response.body() as Map<*, *>).isEmpty())
        return (isEmptyCollection || isEmptyArray || isEmptyMap)
    }

    private fun handleException(throwable: Throwable, responseHandler: (R?, Throwable?) -> Unit) {
        responseHandler(null, throwable)
    }

    private fun handleFailure(response: Response<R>, responseHandler: (R?, Throwable?) -> Unit) {
        responseHandler(null, FailedResponseException("Request failed ${response.raw()}"))
    }

    /**
     * Cancel this call.
     * Uses [Call.cancel]
     */
    fun cancel() {
        call.cancel()
    }

    /**
     * Retry the call.
     * This will retry the request with a new clone [Call.clone] if call was not used for request.
     * Retry will call previously called request method, [execute] if execute was used, [enqueue] if enqueue was used.
     * Make sure the previous call returned, otherwise callback maybe called twice, if no new callback is given
     * @param callback new callback to use for retry, otherwise main callback will be used
     */
    fun retry(callback: SimpleCallback<R> = previousCallback) {
        // there was a request before
        if(whichCall != -1) {
            if(isExecuted()) call = call.clone()
            retry = true
            if (whichCall == EXECUTE) execute(callback)
            else enqueue(callback)
        }
    }

    fun isExecuted(): Boolean = call.isExecuted

    fun isCanceled(): Boolean = call.isCanceled

    fun request(): Request = call.request()

    fun timeout(): Timeout = call.timeout()

    private companion object {
        const val EXECUTE = 1
        const val ENQUEUE = 2
    }
}