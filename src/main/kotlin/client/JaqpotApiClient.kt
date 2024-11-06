package org.jaqpot.client

import client.BaseApiClient
import okhttp3.OkHttpClient
import org.jaqpot.config.SDKConfig
import org.jaqpot.kotlinsdk.api.DatasetApi
import org.jaqpot.kotlinsdk.api.ModelApi
import org.jaqpot.kotlinsdk.model.Dataset
import org.jaqpot.kotlinsdk.model.DatasetType
import org.openapitools.client.infrastructure.ApiResponse

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
            throw Exception("Prediction failed")
        }

        if (dataset!!.status == Dataset.Status.FAILURE) {
            throw Exception("Prediction failed")
        }

        return dataset
    }
}
