package io.github.onreg.core.network.retrofit

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

internal class NetworkResponseCall<Success : Any>(private val delegate: Call<Success>) :
    Call<NetworkResponse<Success>> {
    override fun enqueue(callback: Callback<NetworkResponse<Success>>) {
        delegate.enqueue(
            object : Callback<Success> {
                override fun onResponse(
                    call: Call<Success>,
                    response: Response<Success>,
                ) {
                    callback.onResponse(
                        this@NetworkResponseCall,
                        Response.success(response.toNetworkResponse()),
                    )
                }

                override fun onFailure(
                    call: Call<Success>,
                    throwable: Throwable,
                ) {
                    val networkResponse = when (throwable) {
                        is IOException -> NetworkResponse.Failure.NetworkError(throwable)
                        else -> NetworkResponse.Failure.OtherError(throwable)
                    }
                    callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
                }
            },
        )
    }

    override fun execute(): Response<NetworkResponse<Success>> {
        val result = runCatching { delegate.execute() }
        val response = result.getOrNull()
        val exception = result.exceptionOrNull()

        val networkResponse = when {
            response != null -> response.toNetworkResponse()
            exception is IOException -> NetworkResponse.Failure.NetworkError(exception)
            else -> NetworkResponse.Failure.OtherError(exception)
        }
        return Response.success(networkResponse)
    }

    override fun clone(): Call<NetworkResponse<Success>> = NetworkResponseCall(delegate.clone())

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

    private fun Response<Success>.toNetworkResponse(): NetworkResponse<Success> {
        val body = body()
        return when {
            isSuccessful && body != null -> NetworkResponse.Success(body)
            else -> NetworkResponse.Failure.OtherError(null)
        }
    }
}
