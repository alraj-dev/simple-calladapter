package dev.alraj.simplecalladapter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * Call multiple [SimpleCall] calls and return to a single callback with all the results in List.
 * @param calls List of SimpleCall to call
 */
class MultipleCall<R>(private var calls: List<SimpleCall<R>>) {
    private var countdown = CountDownLatch(calls.size)
    private val results = MutableList<Result<R>?>(calls.size) { null }
    private lateinit var previousCallback: MultipleCallback<R>

    private var retry = false

    /**
     * Call all the [SimpleCall] asynchronously
     * @param callback [MultipleCallback] callback
     */
    fun enqueue(callback: MultipleCallback<R>) {
        if(!retry) previousCallback = callback

        thread(name = "MultipleCall-Thread") {
            val localCallback = SimpleCallback<R> { data, exception, call ->
                val index = calls.indexOf(call)
                results[index] = Result(data, exception, call)
                countdown.countDown()
            }

            for (call in calls)
                if(retry) call.retry() else call.enqueue(localCallback)

            countdown.await()
            retry = false

            CoroutineScope(Dispatchers.Main).launch {
                callback.onResult(
                    results.map { it?.response },
                    results.map { it?.exception },
                    results.map { it?.call },
                    this@MultipleCall
                )
            }
        }
    }

    /**
     * retry all the calls with the previously given callback, if not given new callback.
     * @param calls variable number of SimpleCall to retry, if not given retry all calls.
     * @param callback MultipleCallback to use for the reply, if not given use the previous callback.
     */
    fun retry(vararg retryCalls: SimpleCall<R>, retryCallback: MultipleCallback<R> = previousCallback) {
        countdown = CountDownLatch(retryCalls.size)
        if(retryCalls.isNotEmpty()) {
            calls = retryCalls.toList()
            results.clear()
            results.addAll(calls.map { null })
        }
        retry = true
        enqueue(retryCallback)
    }

    /**
     * Cancel all calls added to the MultipleCall
     */
    fun cancel() {
        for(call in calls) call.cancel()
    }

    data class Result<R> (
        val response: R?,
        val exception: Throwable?,
        val call: SimpleCall<R>
    )
}