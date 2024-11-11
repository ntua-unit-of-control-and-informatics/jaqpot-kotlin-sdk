package client

import auth.AuthorizationInterceptor
import okhttp3.OkHttpClient


open class BaseApiClient {
    companion object {
        fun getHttpClient(apiKey: String, apiSecret: String): OkHttpClient {
            val authorizationInterceptor = AuthorizationInterceptor(apiKey, apiSecret)
            return OkHttpClient.Builder()
                .addInterceptor(authorizationInterceptor)
                .build()
        }
    }
}
