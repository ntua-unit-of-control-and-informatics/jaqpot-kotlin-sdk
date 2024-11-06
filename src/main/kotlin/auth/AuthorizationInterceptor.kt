package auth

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthorizationInterceptor(private val apiKey: String, private val apiSecret: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().signedRequest()
        return chain.proceed(newRequest)
    }

    private fun Request.signedRequest(): Request {
        return this.newBuilder()
            .header("X-Api-Key", apiKey)
            .header("X-Api-Secret", apiSecret)
            .build()
    }
}
