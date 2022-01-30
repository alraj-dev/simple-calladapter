package dev.alraj.simplecalladapter

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * A Retrofit2 call adapter which handles [SimpleCall] Callback
 * @param defaultConditions array of [DefaultConditions] conditions
 */
class SimpleCallAdapter<R>(
    private val responseType: Type,
    private val defaultConditions: Array<DefaultConditions>?
) : CallAdapter<R, Any> {
    override fun responseType(): Type = responseType
    override fun adapt(call: Call<R>): Any {
        return SimpleCall(call, defaultConditions)
    }
}