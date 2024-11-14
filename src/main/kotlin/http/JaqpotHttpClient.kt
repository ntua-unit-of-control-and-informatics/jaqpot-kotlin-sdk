package org.jaqpot.http

import auth.AuthorizationInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.jaqpot.config.SDKConfig
import org.openapitools.client.JSON
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime


class JaqpotHttpClient(
    apiKey: String,
    apiSecret: String
) {

    private val authorizationInterceptor = AuthorizationInterceptor(apiKey, apiSecret)
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authorizationInterceptor)
        .build()

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(OffsetDateTime::class.java, JSON.OffsetDateTimeTypeAdapter())
        .create()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(SDKConfig.host)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


}
