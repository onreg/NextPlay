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
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        val callType = if (getRawType(returnType) == Call::class.java) {
            returnType as? ParameterizedType
        } else {
            null
        }
        val responseType = callType?.let { getParameterUpperBound(0, it) }
        val networkResponseType = if (
            responseType != null && getRawType(responseType) == NetworkResponse::class.java
        ) {
            responseType as? ParameterizedType
        } else {
            null
        }
        val successType = networkResponseType?.let { getParameterUpperBound(0, it) }

        return successType?.let { NetworkResponseCallAdapter<Any>(it) }
    }
}

private class NetworkResponseCallAdapter<Success : Any>(private val successType: Type) :
    CallAdapter<Success, Call<NetworkResponse<Success>>> {
    override fun responseType(): Type = successType

    override fun adapt(call: Call<Success>): Call<NetworkResponse<Success>> =
        NetworkResponseCall(call)
}
