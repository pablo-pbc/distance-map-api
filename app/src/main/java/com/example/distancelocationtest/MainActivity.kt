package com.example.distancelocationtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import kotlinx.coroutines.*
import okhttp3.*

class MainActivity : AppCompatActivity() {

    data class UserAddress(
        val street: String,
        val number: String,
        val city: String,
        val state: String
    )

    data class RestaurantAddress(
        val street: String,
        val number: String,
        val city: String,
        val state: String
    )

    data class DistanceElement(
        val distance: Distance,
        val duration: Duration,
        val status: String
    )

    data class Distance(
        val text: String,
        val value: Int
    )

    data class Duration(
        val text: String,
        val value: Int
    )

    data class DistanceResponse(
        val destination_addresses: List<String>,
        val origin_addresses: List<String>,
        val rows: List<Row>,
        val status: String
    )

    data class Row(
        val elements: List<DistanceElement>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiKey = BuildConfig.MAPS_API_LEY

        val userAddresses = listOf(
            UserAddress("Av Marginal", "2372", "São Paulo", "SP")
            // adicione outros endereços do usuário, se necessário
        )

        val restaurantAddresses = listOf(
            RestaurantAddress("Alameda Terracota", "545", "São Paulo", "SP"),
            RestaurantAddress("R. Dr. Dante Costa", "456", "São Paulo", "SP"),
            RestaurantAddress("Rua Santo Antônio 300", "789", "São Paulo", "SP")
            // adicione outros endereços de restaurantes, se necessário
        )

        val nearbyRestaurants = mutableListOf<RestaurantAddress>()

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        coroutineScope.launch {
            for (userAddress in userAddresses) {
                for (restaurantAddress in restaurantAddresses) {
                    val origin = "${userAddress.street}, ${userAddress.number}, ${userAddress.city}, ${userAddress.state}".replace(" ", "%20")
                    val destination = "${restaurantAddress.street}, ${restaurantAddress.number}, ${restaurantAddress.city}, ${restaurantAddress.state}".replace(" ", "%20")

                    val url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origin&destinations=$destination&units=metric&key=$apiKey"

                    val jsonResponse = fetchJsonResponse(url)

                    val gson = Gson()
                    val response = gson.fromJson(jsonResponse, DistanceResponse::class.java)

                    val distance = response.rows[0].elements[0].distance.value

                    // Verifique se a distância é menor que 10 KM (10,000 metros)
                    if (distance < 1000) {
                        nearbyRestaurants.add(restaurantAddress)
                    }
                }
            }

            // Imprima a lista de restaurantes próximos
            for (restaurant in nearbyRestaurants) {
                Log.d("MainActivity", "${restaurant.street}, ${restaurant.number}, ${restaurant.city}, ${restaurant.state}")
            }
        }
    }

    private suspend fun fetchJsonResponse(url: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}
