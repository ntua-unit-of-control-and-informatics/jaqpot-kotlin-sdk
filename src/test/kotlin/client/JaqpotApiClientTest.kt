package client

import TestUtil.Companion.mockPrivateProperty
import okhttp3.Headers.Companion.toHeaders
import okhttp3.ResponseBody
import org.jaqpot.client.JaqpotApiClient
import org.jaqpot.exception.JaqpotSDKException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.openapitools.client.api.DatasetApi
import org.openapitools.client.api.ModelApi
import org.openapitools.client.model.Dataset
import retrofit2.Call
import retrofit2.Response


class JaqpotApiClientTest {

    private lateinit var modelApi: ModelApi

    private lateinit var datasetApi: DatasetApi

    private lateinit var jaqpotApiClient: JaqpotApiClient

    @BeforeEach
    fun setUp() {
        modelApi = mock(ModelApi::class.java)
        datasetApi = mock(DatasetApi::class.java)
        jaqpotApiClient = JaqpotApiClient("apiKey", "apiSecret")
        mockPrivateProperty(jaqpotApiClient, "modelApi", modelApi)
        mockPrivateProperty(jaqpotApiClient, "datasetApi", datasetApi)
    }


    @Test
    fun `predictSync should return dataset on success`() {
        val dataset = Dataset().apply {
            id = 1L
            status = Dataset.StatusEnum.SUCCESS
        }

        val mockCall = mock(Call::class.java) as Call<Void>
        val mockResponse = mock(Response::class.java) as Response<Void>
        val mockDatasetCall = mock(Call::class.java) as Call<Dataset>
        val mockDatasetResponse = mock(Response::class.java) as Response<Dataset>

        `when`(modelApi.predictWithModel(anyLong(), any(Dataset::class.java))).thenReturn(mockCall)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.headers()).thenReturn(mapOf("Location" to "/datasets/1").toHeaders())
        `when`(datasetApi.getDatasetById(anyLong())).thenReturn(mockDatasetCall)
        `when`(mockDatasetCall.execute()).thenReturn(mockDatasetResponse)
        `when`(mockDatasetResponse.body()).thenReturn(dataset)

        val result = jaqpotApiClient.predictSync(1L, listOf())

        assertEquals(dataset, result)
    }

    @Test
    fun `predictSync should throw exception on failure`() {
        val mockCall = mock(Call::class.java) as Call<Void>
        val mockResponse = mock(Response::class.java) as Response<Void>

        `when`(modelApi.predictWithModel(anyLong(), any(Dataset::class.java))).thenReturn(mockCall)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(mockResponse.isSuccessful).thenReturn(false)
        `when`(mockResponse.errorBody()).thenReturn(mock(ResponseBody::class.java))

        val exception = assertThrows<JaqpotSDKException> {
            jaqpotApiClient.predictSync(1L, listOf())
        }

        assertTrue(exception.message!!.contains("Prediction failed"))
    }

    @Test
    fun `predictSync should throw exception on dataset failure`() {
        val dataset = Dataset().apply {
            id = 1L
            status = Dataset.StatusEnum.FAILURE
            failureReason = "Some failure reason"
        }

        val mockCall = mock(Call::class.java) as Call<Void>
        val mockResponse = mock(Response::class.java) as Response<Void>
        val mockDatasetCall = mock(Call::class.java) as Call<Dataset>
        val mockDatasetResponse = mock(Response::class.java) as Response<Dataset>

        `when`(modelApi.predictWithModel(anyLong(), any(Dataset::class.java))).thenReturn(mockCall)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.headers()).thenReturn(mapOf("Location" to "/datasets/1").toHeaders())
        `when`(datasetApi.getDatasetById(anyLong())).thenReturn(mockDatasetCall)
        `when`(mockDatasetCall.execute()).thenReturn(mockDatasetResponse)
        `when`(mockDatasetResponse.body()).thenReturn(dataset)

        val exception = assertThrows<JaqpotSDKException> {
            jaqpotApiClient.predictSync(1L, listOf())
        }

        assertTrue(exception.message!!.contains("Prediction failed: Some failure reason"))
    }

    @Test
    fun `predictSync should throw exception on maximum retries reached`() {
        val dataset = Dataset().apply {
            id = 1L
            status = Dataset.StatusEnum.EXECUTING
        }

        val mockCall = mock(Call::class.java) as Call<Void>
        val mockResponse = mock(Response::class.java) as Response<Void>
        val mockDatasetCall = mock(Call::class.java) as Call<Dataset>
        val mockDatasetResponse = mock(Response::class.java) as Response<Dataset>

        `when`(modelApi.predictWithModel(anyLong(), any(Dataset::class.java))).thenReturn(mockCall)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.headers()).thenReturn(mapOf("Location" to "/datasets/1").toHeaders())
        `when`(datasetApi.getDatasetById(anyLong())).thenReturn(mockDatasetCall)
        `when`(mockDatasetCall.execute()).thenReturn(mockDatasetResponse)
        `when`(mockDatasetResponse.body()).thenReturn(dataset)

        val exception = assertThrows<JaqpotSDKException> {
            jaqpotApiClient.predictSync(1L, listOf())
        }

        assertTrue(exception.message!!.contains("Maximum amount of retries reached"))
    }
}
