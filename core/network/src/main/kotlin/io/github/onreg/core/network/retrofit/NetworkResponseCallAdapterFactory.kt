package io.github.onreg.core.network.retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

public class NetworkResponseCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        if (returnType !is ParameterizedType) return null

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != NetworkResponse::class.java) return null
        if (responseType !is ParameterizedType) return null

        val successType = getParameterUpperBound(0, responseType)
        return NetworkResponseCallAdapter<Any>(successType)
    }
}

private class NetworkResponseCallAdapter<Success : Any>(
    private val successType: Type
) : CallAdapter<Success, Call<NetworkResponse<Success>>> {
    override fun responseType(): Type = successType

    override fun adapt(call: Call<Success>): Call<NetworkResponse<Success>> = NetworkResponseCall(call)
}

