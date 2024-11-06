package org.jaqpot.client

import client.BaseApiClient
import okhttp3.OkHttpClient
import org.jaqpot.config.SDKConfig
import org.jaqpot.exception.JaqpotSDKException
import org.jaqpot.kotlinsdk.api.DatasetApi
import org.jaqpot.kotlinsdk.api.ModelApi
import org.jaqpot.kotlinsdk.model.Dataset
import org.jaqpot.kotlinsdk.model.DatasetType
import org.openapitools.client.infrastructure.ApiResponse
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ResponseType

class JaqpotApiClient(
    private val apiKey: String,
    private val apiSecret: String
) : BaseApiClient() {

    private val baseUrl: String = SDKConfig.host
    private val httpClient: OkHttpClient = getApiClient(apiKey, apiSecret)
    private val modelApi: ModelApi = ModelApi(baseUrl, httpClient)
    private val datasetApi: DatasetApi = DatasetApi(baseUrl, httpClient)

    suspend fun predictAsync(modelId: Long, input: List<Any>): ApiResponse<Unit?> {
        return modelApi.predictWithModelWithHttpInfo(
            modelId,
            Dataset(type = DatasetType.PREDICTION, entryType = Dataset.EntryType.ARRAY, input)
        )
    }

    suspend fun predictSync(modelId: Long, input: List<Any>): Dataset {
        val response = modelApi.predictWithModelWithHttpInfo(
            modelId,
            Dataset(type = DatasetType.PREDICTION, entryType = Dataset.EntryType.ARRAY, input)
        )

        if (response.responseType == ResponseType.ClientError) {
            if (response.statusCode === 403) {
                throw JaqpotSDKException("Prediction failed: Unauthorized")
            } else if (response.statusCode === 404) {
                throw JaqpotSDKException("Prediction failed: Model not found")
            }

            val body: Any? = (response as ClientError).body
            throw JaqpotSDKException("Prediction failed: ${body.toString()}")
        }

        val datasetLocation = response.headers["Location"]!!.first()
        val datasetId = datasetLocation.substringAfterLast("/").toLong()

        var dataset: Dataset? = null

        var retries = 0
        while (retries < 10) {
            dataset = datasetApi.getDatasetById(datasetId)
            if (dataset.status == Dataset.Status.SUCCESS || dataset.status == Dataset.Status.FAILURE) {
                break
            }
            Thread.sleep(2000)

            retries++
        }

        if (retries >= 10) {
            throw JaqpotSDKException("Prediction failed: Maximum amount of retries reached")
        }

        if (dataset!!.status == Dataset.Status.FAILURE) {
            throw JaqpotSDKException("Prediction failed")
        }

        return dataset
    }
}
