package client

import org.jaqpot.client.JaqpotApiClient
import org.jaqpot.config.SDKConfig
import org.jaqpot.exception.JaqpotSDKException
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openapitools.client.model.ApiKey
import org.openapitools.client.model.CreateApiKey201Response

/**
 * Integration test for API key creation functionality.
 * 
 * This test calls the real Jaqpot API and demonstrates the createApiKey() method.
 * 
 * Required environment variables:
 * - JAQPOT_API_KEY: Your API key
 * - JAQPOT_API_SECRET: Your API secret
 * - JAQPOT_HOST (optional): API host URL (defaults to https://api.jaqpot.org, use http://localhost:8080 for local)
 * 
 * Note: The API keys used must be valid and not expired to successfully create new API keys.
 */
class ApiKeyCreationIntegrationTest {

    @Test
    @Disabled("Integration test - requires valid API credentials and should be run manually")
    fun `test API key creation with different configurations`() {
        // Get credentials from environment variables
        val apiKey = System.getenv("JAQPOT_API_KEY")
        val apiSecret = System.getenv("JAQPOT_API_SECRET")
        val host = System.getenv("JAQPOT_HOST") ?: "https://api.jaqpot.org"

        if (apiKey.isNullOrBlank() || apiSecret.isNullOrBlank()) {
            val errorMessage = """
                ❌ Missing required environment variables!
                
                Please set the following environment variables:
                • JAQPOT_API_KEY="your_api_key"
                • JAQPOT_API_SECRET="your_api_secret" 
                • JAQPOT_HOST="http://localhost:8080" (optional, for local testing)
                
                Example:
                export JAQPOT_API_KEY="your_api_key"
                export JAQPOT_API_SECRET="your_api_secret"
            """.trimIndent()
            
            println(errorMessage)
            throw IllegalStateException("Missing required environment variables")
        }

        // Configure SDK host
        SDKConfig.host = host
        println("🔧 Using API host: $host")
        println("🔑 API Key: ${apiKey.take(8)}...")
        
        // Initialize client
        val client = JaqpotApiClient(apiKey, apiSecret)
        println("✅ JaqpotApiClient initialized successfully\n")

        runAllApiKeyCreationTests(client)
    }

    /**
     * Runs all API key creation test scenarios
     */
    private fun runAllApiKeyCreationTests(client: JaqpotApiClient) {
        println("🚀 Starting API key creation tests...\n")
        
        val results = mutableListOf<TestResult>()
        
        // Test 1: Default settings
        results.add(testDefaultApiKeyCreation(client))
        
        // Test 2: Custom settings (6 months + note)
        results.add(testCustomApiKeyCreation(client))
        
        // Test 3: Note only
        results.add(testApiKeyWithNoteOnly(client))
        
        printTestSummary(results)
    }

    /**
     * Data class to track test results
     */
    private data class TestResult(
        val testName: String,
        val success: Boolean,
        val message: String,
        val apiKey: String? = null
    )

    /**
     * Test 1: Create API key with default settings (3 months, no note)
     */
    private fun testDefaultApiKeyCreation(client: JaqpotApiClient): TestResult {
        println("📝 Test 1: Creating API key with default settings (3 months, no note)")
        return try {
            val response: CreateApiKey201Response = client.createApiKey()
            val successMsg = "✅ API key created successfully"
            println("   $successMsg")
            println("   📋 New API Key: ${response.clientKey}")
            println("   🔐 New API Secret: ${response.clientSecret?.let { "${it.take(8)}..." } ?: "null"}")
            TestResult("Default Settings", true, successMsg, response.clientKey)
        } catch (e: JaqpotSDKException) {
            val errorMsg = "Failed: ${e.message}"
            println("   ❌ $errorMsg")
            TestResult("Default Settings", false, errorMsg)
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("   ❌ $errorMsg")
            TestResult("Default Settings", false, errorMsg)
        }
    }

    /**
     * Test 2: Create API key with custom settings (6 months + note)
     */
    private fun testCustomApiKeyCreation(client: JaqpotApiClient): TestResult {
        println("\n📝 Test 2: Creating API key with custom settings (6 months + note)")
        return try {
            val response: CreateApiKey201Response = client.createApiKey(
                note = "Test API key created from Kotlin integration test",
                expirationTime = ApiKey.ExpirationTimeEnum.SIX_MONTHS
            )
            val successMsg = "✅ API key created successfully with 6-month expiration"
            println("   $successMsg")
            println("   📋 New API Key: ${response.clientKey}")
            println("   🔐 New API Secret: ${response.clientSecret?.let { "${it.take(8)}..." } ?: "null"}")
            TestResult("Custom Settings (6 months + note)", true, successMsg, response.clientKey)
        } catch (e: JaqpotSDKException) {
            val errorMsg = "Failed: ${e.message}"
            println("   ❌ $errorMsg")
            TestResult("Custom Settings (6 months + note)", false, errorMsg)
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("   ❌ $errorMsg")
            TestResult("Custom Settings (6 months + note)", false, errorMsg)
        }
    }

    /**
     * Test 3: Create API key with note only (defaults to 3 months)
     */
    private fun testApiKeyWithNoteOnly(client: JaqpotApiClient): TestResult {
        println("\n📝 Test 3: Creating API key with note only (defaults to 3 months)")
        return try {
            val response: CreateApiKey201Response = client.createApiKey(
                note = "API key with note only - Kotlin test"
            )
            val successMsg = "✅ API key created successfully with default 3-month expiration"
            println("   $successMsg")
            println("   📋 New API Key: ${response.clientKey}")
            println("   🔐 New API Secret: ${response.clientSecret?.let { "${it.take(8)}..." } ?: "null"}")
            TestResult("Note Only (3 months default)", true, successMsg, response.clientKey)
        } catch (e: JaqpotSDKException) {
            val errorMsg = "Failed: ${e.message}"
            println("   ❌ $errorMsg")
            TestResult("Note Only (3 months default)", false, errorMsg)
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("   ❌ $errorMsg")
            TestResult("Note Only (3 months default)", false, errorMsg)
        }
    }

    /**
     * Prints a summary of all test results
     */
    private fun printTestSummary(results: List<TestResult>) {
        println("\n" + "=".repeat(60))
        println("📊 TEST SUMMARY")
        println("=".repeat(60))
        
        val successCount = results.count { it.success }
        val totalCount = results.size
        
        results.forEachIndexed { index, result ->
            val status = if (result.success) "✅ PASS" else "❌ FAIL"
            println("${index + 1}. ${result.testName}: $status")
            if (result.success && result.apiKey != null) {
                println("   Created API Key: ${result.apiKey}")
            }
        }
        
        println("\n📈 Results: $successCount/$totalCount tests passed")
        
        if (successCount == totalCount) {
            println("🎉 All tests passed! API key creation is working correctly.")
        } else {
            println("⚠️  Some tests failed. Please check the error messages above.")
        }
        
        println("=".repeat(60))
    }
}
