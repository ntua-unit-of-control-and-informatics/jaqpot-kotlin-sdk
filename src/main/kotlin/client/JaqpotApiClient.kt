package org.jaqpot.client

import org.jaqpot.exception.JaqpotSDKException
import org.jaqpot.http.JaqpotHttpClient
import org.openapitools.client.api.ApiKeysApi
import org.openapitools.client.api.DatasetApi
import org.openapitools.client.api.ModelApi
import org.openapitools.client.model.ApiKey
import org.openapitools.client.model.CreateApiKey201Response
import org.openapitools.client.model.Dataset
import org.openapitools.client.model.DatasetType


class JaqpotApiClient(apiKey: String, apiSecret: String) {

    private val jaqpotHttpClient: JaqpotHttpClient = JaqpotHttpClient(apiKey, apiSecret)
    val modelApi: ModelApi = jaqpotHttpClient.retrofit.create(ModelApi::class.java)
    val datasetApi: DatasetApi = jaqpotHttpClient.retrofit.create(DatasetApi::class.java)
//    val featureApi: FeatureApi = jaqpotHttpClient.retrofit.create(FeatureApi::class.java)
    val apiKeysApi: ApiKeysApi = jaqpotHttpClient.retrofit.create(ApiKeysApi::class.java)

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

    /**
     * Creates a new API key pair for the authenticated user.
     * 
     * This method uses the current API key/secret authentication to create new API keys.
     * The API keys used for authentication must be valid and not expired.
     * 
     * @param note Optional note to describe the API key
     * @param expirationTime Expiration time (THREE_MONTHS or SIX_MONTHS, defaults to THREE_MONTHS)
     * @return CreateApiKey201Response containing the new API key and secret
     * @throws JaqpotSDKException if the API key creation fails
     */
    fun createApiKey(
        note: String? = null,
        expirationTime: ApiKey.ExpirationTimeEnum = ApiKey.ExpirationTimeEnum.THREE_MONTHS
    ): CreateApiKey201Response {
        val apiKeyRequest = ApiKey.Builder()
            .expirationTime(expirationTime)
            .apply { note?.let { note(it) } }
            .build()

        val response = apiKeysApi.createApiKey(apiKeyRequest).execute()

        if (!response.isSuccessful) {
            val message = response.errorBody()?.string()
            when (response.code()) {
                401 -> throw JaqpotSDKException("API key creation failed: Unauthenticated \n$message", response.errorBody())
                403 -> throw JaqpotSDKException("API key creation failed: Unauthorized \n$message", response.errorBody())
                else -> throw JaqpotSDKException("API key creation failed: $message", response.errorBody())
            }
        }

        return response.body() ?: throw JaqpotSDKException("API key creation failed: Empty response body")
    }

}
