package org.jaqpot.client

import org.jaqpot.exception.JaqpotSDKException
import org.jaqpot.http.JaqpotHttpClient
import org.openapitools.client.api.DatasetApi
import org.openapitools.client.api.ModelApi
import org.openapitools.client.model.Dataset
import org.openapitools.client.model.DatasetType


class JaqpotApiClient(private val apiKey: String, private val apiSecret: String) {

    private val jaqpotHttpClient: JaqpotHttpClient = JaqpotHttpClient(apiKey, apiSecret)
    private val modelApi: ModelApi = jaqpotHttpClient.retrofit.create(ModelApi::class.java)
    private val datasetApi: DatasetApi = jaqpotHttpClient.retrofit.create(DatasetApi::class.java)

    companion object {
        const val DATASET_CHECK_INTERVAL: Long = 2000
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
            val message = response.errorBody()?.string()
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
            throw JaqpotSDKException("Prediction failed: ${dataset.failureReason}")
        }

        return dataset
    }

}
