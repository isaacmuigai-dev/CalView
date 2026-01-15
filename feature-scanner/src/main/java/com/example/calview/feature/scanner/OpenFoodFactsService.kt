package com.example.calview.feature.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service to lookup product nutritional information from OpenFoodFacts API.
 * This is a free API that doesn't require an API key.
 */
object OpenFoodFactsService {
    
    private const val BASE_URL = "https://world.openfoodfacts.org/api/v0/product"
    
    /**
     * Look up a product by barcode.
     * Returns ProductInfo if found, null otherwise.
     */
    suspend fun getProductByBarcode(barcode: String): ProductInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "CalViewAI/1.0 (Android)")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseResponse(response)
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("OpenFoodFacts", "Error fetching product: ${e.message}", e)
            null
        }
    }
    
    private fun parseResponse(json: String): ProductInfo? {
        return try {
            val root = JSONObject(json)
            val status = root.optInt("status", 0)
            
            if (status != 1) {
                return null // Product not found
            }
            
            val product = root.optJSONObject("product") ?: return null
            val nutriments = product.optJSONObject("nutriments")
            
            val name = product.optString("product_name", "").ifEmpty {
                product.optString("product_name_en", "Unknown Product")
            }
            
            // Get nutritional values per 100g or per serving
            val calories = nutriments?.optDouble("energy-kcal_100g", 0.0)?.toInt()
                ?: nutriments?.optDouble("energy-kcal", 0.0)?.toInt()
                ?: 0
            
            val protein = nutriments?.optDouble("proteins_100g", 0.0)?.toFloat()
                ?: nutriments?.optDouble("proteins", 0.0)?.toFloat()
                ?: 0f
                
            val carbs = nutriments?.optDouble("carbohydrates_100g", 0.0)?.toFloat()
                ?: nutriments?.optDouble("carbohydrates", 0.0)?.toFloat()
                ?: 0f
                
            val fats = nutriments?.optDouble("fat_100g", 0.0)?.toFloat()
                ?: nutriments?.optDouble("fat", 0.0)?.toFloat()
                ?: 0f
            
            val servingSize = product.optString("serving_size", "")
            
            val brands = product.optString("brands", "")
            val fullName = if (brands.isNotEmpty() && !name.contains(brands, ignoreCase = true)) {
                "$brands $name"
            } else {
                name
            }
            
            ProductInfo(
                name = fullName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                servingSize = servingSize,
                barcode = product.optString("code", "")
            )
        } catch (e: Exception) {
            android.util.Log.e("OpenFoodFacts", "Error parsing response: ${e.message}", e)
            null
        }
    }
}

/**
 * Product nutritional information from OpenFoodFacts.
 * Values are per 100g unless servingSize is specified.
 */
data class ProductInfo(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val servingSize: String?,
    val barcode: String
)
