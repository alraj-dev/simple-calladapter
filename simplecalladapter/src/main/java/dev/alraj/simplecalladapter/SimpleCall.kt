package dev.alraj.simplecalladapter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private var lifecycleOwner: LifecycleOwner? = null

    private var tempDefaultConditions: MutableList<DefaultConditions> =
        defaultConditions?.toMutableList() ?: mutableListOf()

    private var retry = false
    private lateinit var previousCallback: SimpleCallback<R>

    private var reportCancel = false

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
     * Cancel the network call when the lifecycle reaches the given cancelState
     * @param owner LifecycleOwner of activity, fragment, view, etc
     * @param state State in which to cancel the call, default is Destroyed
     * @param report Whether to report lifecycleScope cancel like normal cancel with IOException, default is false
     */
    fun lifecycle(
        owner: LifecycleOwner,
        state: Lifecycle.State = Lifecycle.State.DESTROYED,
        report: Boolean = false
    ): SimpleCall<R> {
        reportCancel = report
        lifecycleOwner = owner
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event.targetState == state) {
                    // cancel call when lifecycle owner reaches cancelState
                    call.cancel()

                    // remove the observer, no longer need it
                    source.lifecycle.removeObserver(this)
                }
            }
        })
        return this
    }

    /**
     * Run this request Synchronously in the same thread. Uses [Call.execute]
     * @return The deserialized object
     */
    @Throws(Throwable::class)
    fun execute(): R? {
        val pair = handleBody(call.execute())
        throw pair.second ?: return pair.first
    }

    /**
     * Run this request Asynchronously in the background thread. Uses [Call.enqueue]
     * Return to the callback in the same thread called it.
     * @param callback [SimpleCallback] implementation which gives data and exception
     */
    fun enqueue(callback: SimpleCallback<R>) {
        reportCancel = false
        if (!retry) previousCallback = callback

        // a wrapper lambda to run the user callback in main thread
        val wrapperHandler: (R?, Throwable?) -> Unit = { body: R?, exception: Throwable? ->
            // run the callback in main thread
            CoroutineScope(Dispatchers.Main).launch {
                // If call is canceled and should not be reported with onFailure, do nothing
                if (call.isCanceled && !reportCancel) return@launch
                callback.onResult(body, exception, this@SimpleCall)
            }
        }

        call.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>?, r: Response<R>) {
                val pair = handleResponse(r)
                wrapperHandler(pair.first, pair.second)
            }

            override fun onFailure(call: Call<R>?, t: Throwable) {
                val pair = handleException(t)
                wrapperHandler(pair.first, pair.second)
            }
        })
        retry = false
    }

    private fun handleResponse(response: Response<R>): Pair<R?, Throwable?> {
        // if request is success, the response will have a body
        return if (response.isSuccessful) {
            handleBody(response)
        }
        // otherwise handle failure state
        else {
            handleFailure(response)
        }
    }

    private fun handleBody(response: Response<R>): Pair<R?, Throwable?> {
        // Response body is not null
        return if (response.body() != null) {
            handleData(response)
        }
        // Response body is null and Flagged to transfer to FailureCall
        else if (DefaultConditions.NULL_RESPONSE in tempDefaultConditions) {
            Pair(
                null,
                NullDataException("Null body in client response, ${response.raw()}")
            )
        }
        // Response body is null, still give it to user
        else {
            Pair(null, null)
        }
    }

    private fun handleData(response: Response<R>): Pair<R?, Throwable?> {
        // Response body is empty and flagged
        return if (isEmpty(response) && DefaultConditions.EMPTY_LIST in tempDefaultConditions) {
            Pair(
                null,
                EmptyListException("Response body is an empty collection ${response.raw()}")
            )
        }
        // Response body is a class data or non empty or empty and not flagged
        else {
            Pair(response.body(), null)
        }
    }

    private fun isEmpty(response: Response<R>): Boolean {
        val isEmptyCollection =
            (response.body() is Collection<*> && (response.body() as Collection<*>).isEmpty())
        val isEmptyArray = (response.body() is Array<*> && (response.body() as Array<*>).isEmpty())
        val isEmptyMap = (response.body() is Map<*, *> && (response.body() as Map<*, *>).isEmpty())
        return (isEmptyCollection || isEmptyArray || isEmptyMap)
    }

    private fun handleFailure(response: Response<R>): Pair<R?, Throwable?> {
        return Pair(null, FailedResponseException(response.code(), "Request failed ${response.raw()}"))
    }

    private fun handleException(throwable: Throwable): Pair<R?, Throwable?> {
        return Pair(null, throwable)
    }

    /**
     * Cancel this call.
     * Uses [Call.cancel]
     */
    fun cancel() {
        reportCancel = true
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
        if (isExecuted()) call = call.clone()
        retry = true
        enqueue(callback)
    }

    fun isExecuted(): Boolean = call.isExecuted

    fun isCanceled(): Boolean = call.isCanceled

    fun request(): Request = call.request()

    fun timeout(): Timeout = call.timeout()

    private val thread: Thread = Thread.currentThread()
}