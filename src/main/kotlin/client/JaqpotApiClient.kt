package org.jaqpot.client

import client.BaseApiClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jaqpot.config.SDKConfig
import org.jaqpot.exception.JaqpotSDKException
import org.openapitools.client.JSON.OffsetDateTimeTypeAdapter
import org.openapitools.client.api.DatasetApi
import org.openapitools.client.api.ModelApi
import org.openapitools.client.model.Dataset
import org.openapitools.client.model.DatasetType
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime


class JaqpotApiClient(
    private val apiKey: String,
    private val apiSecret: String,
    private val baseUrl: String = SDKConfig.host,
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeTypeAdapter())
        .create(),
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(getHttpClient(apiKey, apiSecret))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build(),
    private val modelApi: ModelApi = retrofit.create(ModelApi::class.java),
    private val datasetApi: DatasetApi = retrofit.create(DatasetApi::class.java)
) : BaseApiClient() {

    companion object {
        const val DATASET_CHECK_INTERVAL: Long = 2000
    }

    fun predictAsync(modelId: Long, input: List<Any>): Call<Void>? {
        return modelApi.predictWithModel(
            modelId,
            Dataset.Builder().type(DatasetType.PREDICTION)
                .entryType(Dataset.EntryTypeEnum.ARRAY)
                .input(input)
                .build()
        )
    }

    fun predictSync(modelId: Long, input: List<Any>): Dataset {
        val response = modelApi.predictWithModel(
            modelId,
            Dataset.Builder().type(DatasetType.PREDICTION)
                .entryType(Dataset.EntryTypeEnum.ARRAY)
                .input(input)
                .build()
        ).execute()

        if (!response.isSuccessful) {
            val message = response.errorBody()?.string();
            if (response.code() == 401) {
                throw JaqpotSDKException("Prediction failed: Unauthenticated \n$message", response.errorBody())
            } else if (response.code() == 403) {
                throw JaqpotSDKException("Prediction failed: Unauthorized \n$message", response.errorBody())
            } else if (response.code() == 404) {
                throw JaqpotSDKException("Prediction failed: Model not found\n$message", response.errorBody())
            }

            throw JaqpotSDKException("Prediction failed: $message", response.errorBody())
        }

        val datasetLocation = response.headers()["Location"]!!
        val datasetId = datasetLocation.substringAfterLast("/").toLong()

        var dataset: Dataset? = null

        var retries = 0
        while (retries < 10) {
            dataset = datasetApi.getDatasetById(datasetId).execute().body()
            if (dataset == null) {
                throw JaqpotSDKException("Prediction failed: Dataset not found")
            }
            if (dataset.status == Dataset.StatusEnum.SUCCESS || dataset.status == Dataset.StatusEnum.FAILURE) {
                break
            }

            Thread.sleep(DATASET_CHECK_INTERVAL)

            retries++
        }

        if (retries >= 10) {
            throw JaqpotSDKException("Prediction failed: Maximum amount of retries reached")
        }

        if (dataset!!.status == Dataset.StatusEnum.FAILURE) {
            throw JaqpotSDKException("Prediction failed")
        }

        return dataset
    }


}
