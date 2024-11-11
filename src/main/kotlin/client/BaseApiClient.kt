package client

import auth.AuthorizationInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.jaqpot.config.SDKConfig
import org.openapitools.client.JSON
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime


open class BaseApiClient protected constructor(
    private val apiKey: String,
    private val apiSecret: String,
    private val baseUrl: String = SDKConfig.host
) {

    private val authorizationInterceptor = AuthorizationInterceptor(apiKey, apiSecret)
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authorizationInterceptor)
        .build()

    protected val gson: Gson = GsonBuilder()
        .registerTypeAdapter(OffsetDateTime::class.java, JSON.OffsetDateTimeTypeAdapter())
        .create()

    protected val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


}
