package com.example.trixpress

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class TricycleDriver(
    val name: String,
    val toda: String,
    val location: LatLng,
    val fare: String
)

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var activeRoutePolyline: Polyline? = null

    private val passengerLocation = LatLng(14.0689, 120.6295)
    private val sampleDestination = LatLng(14.0725, 120.6335)

    private val allDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", LatLng(14.0667, 120.6261), "₱40.00"),
        TricycleDriver("Kuya Lito", "ARASOF Campus TODA", LatLng(14.0667, 120.6261), "₱40.00"),
        TricycleDriver("Kuya Cardo", "ARASOF Campus TODA", LatLng(14.0667, 120.6261), "₱40.00"),
        TricycleDriver("Kuya Juaning", "Town Proper TODA", LatLng(14.0743, 120.6325), "₱45.00"),
        TricycleDriver("Kuya Dodong", "Town Proper TODA", LatLng(14.0743, 120.6325), "₱45.00"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", LatLng(14.0951688, 120.6483039), "₱50.00"),
        TricycleDriver("Kuya Ben", "Bunducan TODA", LatLng(14.0951688, 120.6483039), "₱50.00"),
        TricycleDriver("Kuya Nilo", "Wawa TODA", LatLng(14.09945, 120.64394), "₱60.00"),
        TricycleDriver("Kuya Mark", "Natipuan Beach TODA", LatLng(14.1283, 120.6019), "₱80.00")
    )

    private var nearbyDrivers = listOf<TricycleDriver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        nearbyDrivers = filterDriversByDistance(
            drivers = allDrivers,
            origin = passengerLocation,
            maxDistanceMeters = 1500f
        )

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnZones = findViewById<Button>(R.id.btnZones)
        val btnActiveDrivers = findViewById<Button>(R.id.btnActiveDrivers)

        btnZones.setOnClickListener {
            Toast.makeText(this, "Nasugbu Service Zones boundary displayed", Toast.LENGTH_SHORT).show()
        }

        btnActiveDrivers.setOnClickListener {
            if (nearbyDrivers.isEmpty()) {
                Toast.makeText(this, "No active drivers available nearby", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val displayList = nearbyDrivers.map { "${it.name}\n${it.toda} (${it.fare})" }.toTypedArray()

            android.app.AlertDialog.Builder(this)
                .setTitle("Nearby TODA Drivers Only")
                .setItems(displayList) { _, which ->
                    val selectedDriver = nearbyDrivers[which]

                    lifecycleScope.launch {
                        activeRoutePolyline = drawRoadRoute(
                            map = mMap,
                            origin = selectedDriver.location,
                            destination = passengerLocation,
                            apiKey = getMapsApiKey(),
                            existingPolyline = activeRoutePolyline
                        )
                    }

                    val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
                        putExtra("DRIVER_NAME", "${selectedDriver.name} (${selectedDriver.toda})")
                        putExtra("PICKUP_LOC", "BatStateU ARASOF Campus")
                        putExtra("DEST_LOC", "Nasugbu Public Market")
                        putExtra("EST_FARE", selectedDriver.fare)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15f))

        mMap.addMarker(
            MarkerOptions()
                .position(passengerLocation)
                .title("Your Pickup Location")
                .snippet("BatStateU ARASOF Campus")
        )

        for (driver in nearbyDrivers) {
            mMap.addMarker(
                MarkerOptions()
                    .position(driver.location)
                    .title("${driver.name} • ${driver.toda}")
                    .snippet("Fare: ${driver.fare}")
            )
        }

        lifecycleScope.launch {
            activeRoutePolyline = drawRoadRoute(
                map = mMap,
                origin = passengerLocation,
                destination = sampleDestination,
                apiKey = getMapsApiKey(),
                existingPolyline = activeRoutePolyline
            )
        }
    }

    private fun getMapsApiKey(): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private suspend fun drawRoadRoute(
        map: GoogleMap,
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        existingPolyline: Polyline? = null
    ): Polyline? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext existingPolyline

        val url = "https://routes.googleapis.com/directions/v2:computeRoutes"

        val requestJson = """
            {
              "origin": {
                "location": {
                  "latLng": {
                    "latitude": ${origin.latitude},
                    "longitude": ${origin.longitude}
                  }
                }
              },
              "destination": {
                "location": {
                  "latLng": {
                    "latitude": ${destination.latitude},
                    "longitude": ${destination.longitude}
                  }
                }
              },
              "travelMode": "DRIVE",
              "routingPreference": "TRAFFIC_AWARE"
            }
        """.trimIndent()

        val client = OkHttpClient()
        val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", apiKey)
            .addHeader("X-Goog-FieldMask", "routes.polyline.encodedPolyline")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext existingPolyline
            if (!response.isSuccessful) {
                android.util.Log.e("RoutesAPI", "HTTP ${response.code}: $responseBody")
                return@withContext existingPolyline
            }

            val json = JSONObject(responseBody)
            val routes = json.optJSONArray("routes") ?: return@withContext existingPolyline
            if (routes.length() == 0) return@withContext existingPolyline

            val encodedPoints = routes
                .getJSONObject(0)
                .getJSONObject("polyline")
                .getString("encodedPolyline")

            val decodedPoints = PolyUtil.decode(encodedPoints)

            withContext(Dispatchers.Main) {
                existingPolyline?.remove()
                map.addPolyline(
                    PolylineOptions()
                        .addAll(decodedPoints)
                        .width(12f)
                        .color("#1A237E".toColorInt())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            existingPolyline
        }
    }

    private fun filterDriversByDistance(
        drivers: List<TricycleDriver>,
        origin: LatLng,
        maxDistanceMeters: Float
    ): List<TricycleDriver> {
        val filteredList = mutableListOf<TricycleDriver>()
        val results = FloatArray(1)

        for (driver in drivers) {
            Location.distanceBetween(
                origin.latitude, origin.longitude,
                driver.location.latitude, driver.location.longitude,
                results
            )
            if (results[0] <= maxDistanceMeters) {
                filteredList.add(driver)
            }
        }
        return filteredList
    }
}