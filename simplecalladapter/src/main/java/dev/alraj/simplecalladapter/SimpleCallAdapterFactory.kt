package dev.alraj.simplecalladapter

import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class SimpleCallAdapterFactory
private constructor(
    private val toFailure: Array<DefaultConditions>?,
) : CallAdapter.Factory() {

    private constructor() : this(null)

    override fun get(
        returnType: Type?,
        annotations: Array<out Annotation>?,
        retrofit: Retrofit?
    ): CallAdapter<*, *>? {
        return returnType?.let {
            return try {
                // get enclosing type
                val enclosingType = (it as ParameterizedType)

                // ensure enclosing type is 'Simple'
                if (enclosingType.rawType != SimpleCall::class.java) {
                    null
                } else {
                    val type = enclosingType.actualTypeArguments[0]
                    SimpleCallAdapter<Any>(type, toFailure)
                }
            } catch (ex: ClassCastException) {
                null
            }
        }
    }

    companion object {
        /**
         * create [SimpleCallAdapter] retrofit2 call adapter with no default [DefaultConditions]
         */
        @JvmStatic
        fun create() = SimpleCallAdapterFactory()

        /**
         * create Simple retrofit2 call adapter with default conditions [DefaultConditions]
         * @param defaultConditions array of [DefaultConditions]
         */
        @JvmStatic
        fun create(defaultConditions: Array<DefaultConditions>?) =
            SimpleCallAdapterFactory(defaultConditions)
    }
}