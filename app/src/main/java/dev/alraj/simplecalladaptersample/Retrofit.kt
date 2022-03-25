package dev.alraj.simplecalladaptersample

import ServiceInterceptor
import dev.alraj.simplecalladapter.BuildConfig
import dev.alraj.simplecalladapter.DefaultConditions
import dev.alraj.simplecalladapter.SimpleCall
import dev.alraj.simplecalladapter.SimpleCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            if(BuildConfig.DEBUG)
                addInterceptor(ServiceInterceptor())
            callTimeout(60, TimeUnit.SECONDS)
        }.build()
    }

    val retrofitService: RetrofitService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("http://192.168.119.185:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(
                SimpleCallAdapterFactory.create(
                    arrayOf(DefaultConditions.NULL_RESPONSE)
                )
            )
            .build()
            .create(RetrofitService::class.java)
    }
}

interface RetrofitService {
    @GET("user")
    fun user(): SimpleCall<UserResponse>

    @GET("users")
    fun users(): SimpleCall<Base<List<User>>>

    @GET("cat")
    fun cat(): SimpleCall<Base<Cat>>

    @GET("software")
    fun software(): SimpleCall<Base<Software>>

    @GET("success")
    fun success(): SimpleCall<Base<String>>
}