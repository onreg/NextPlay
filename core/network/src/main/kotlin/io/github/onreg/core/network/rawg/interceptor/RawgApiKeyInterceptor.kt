package io.github.onreg.core.network.rawg.interceptor

import androidx.annotation.VisibleForTesting
import io.github.onreg.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

@VisibleForTesting
public const val QUERY_PARAMETER_KEY: String = "key"

public class RawgApiKeyInterceptor(private val apiKey: String = BuildConfig.RAWG_API_KEY) :
    Interceptor {
    init {
        require(apiKey.isNotBlank()) { "API key must not be blank!" }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
            .newBuilder()
            .addQueryParameter(QUERY_PARAMETER_KEY, apiKey)
            .build()
        val updatedRequest = original.newBuilder().url(url).build()
        return chain.proceed(updatedRequest)
    }
}
