package io.github.onreg.core.network.rawg.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class RawgApiKeyInterceptorTest {

    private val expectedKey = "testKey"
    private val apiKeyInterceptor = RawgApiKeyInterceptor(expectedKey)

    @Test
    fun `should add api key query parameter to request`() {
        val chain = mock<Interceptor.Chain>() {
            on { request() } doReturn Request.Builder()
                .url("https://example.com/some-endpoint")
                .build()
            on { proceed(any()) } doReturn mock<Response>()
        }

        apiKeyInterceptor.intercept(chain)

        val argumentCaptor = argumentCaptor<Request>()
        verify(chain).proceed(argumentCaptor.capture())

        val key = argumentCaptor.firstValue.url.queryParameter(QUERY_PARAMETER_KEY)
        assertEquals(expectedKey, key)
    }
}