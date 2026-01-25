package io.github.onreg.core.network.retrofit

import java.io.IOException

public sealed interface NetworkResponse<out SuccessResponse> {
    public data class Success<T>(val body: T) : NetworkResponse<T>

    public sealed interface Failure : NetworkResponse<Nothing> {
        public val exception: Throwable?

        public data class NetworkError(override val exception: IOException) : Failure

        public data class OtherError(override val exception: Throwable?) : Failure
    }
}
