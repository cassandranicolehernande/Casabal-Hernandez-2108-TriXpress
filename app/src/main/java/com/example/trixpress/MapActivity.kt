package com.example.trixpress

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    val fare: String,
    val tricycleType: String = "Standard Sidecar",
    val unitModel: String = "Honda TMX 125 Alpha",
    val seatCapacity: Int = 4,
    val plateNumber: String = "412-ABC"
)

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var activeRoutePolyline: Polyline? = null

    private val passengerLocation = LatLng(14.0673, 120.6262)
    private val sampleDestination = LatLng(14.0754, 120.6358)

    private val allDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", LatLng(14.0673, 120.6262), "₱40.00", "Standard Sidecar", "Honda TMX 125 Alpha", 4, "412-ABC"),
        TricycleDriver("Kuya Lito", "Town Proper TODA", LatLng(14.0736, 120.6322), "₱40.00", "Extended Family Sidecar", "Kawasaki Barako II 175", 6, "582-DEF"),
        TricycleDriver("Kuya Cardo", "Town Plaza TODA", LatLng(14.0731, 120.6320), "₱40.00", "Center-Car / Bajaj RE", "Bajaj RE 205", 4, "913-GHI"),
        TricycleDriver("Kuya Juaning", "Public Market TODA", LatLng(14.0754, 120.6358), "₱45.00", "Standard Sidecar", "Honda TMX Supremo 150", 4, "204-JKL"),
        TricycleDriver("Kuya Dodong", "Jollibee Crossing TODA", LatLng(14.0744, 120.6346), "₱45.00", "Standard Sidecar", "Yamaha YTX 125", 4, "615-MNO"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", LatLng(14.0948, 120.6480), "₱50.00", "Extended Family Sidecar", "Kawasaki Barako II 175", 6, "731-PQR"),
        TricycleDriver("Kuya Nilo", "Wawa Port TODA", LatLng(14.0845, 120.6248), "₱55.00", "E-Trike Passenger Unit", "BEMAC E-Trike", 6, "846-STU"),
        TricycleDriver("Kuya Mark", "Natipuan TODA", LatLng(14.1283, 120.6019), "₱60.00", "Center-Car / TVS King", "TVS King Deluxe", 4, "319-VWX"),
        TricycleDriver("Kuya Jun", "Calayo Beach TODA", LatLng(14.1485, 120.6115), "₱65.00", "Extended Family Sidecar", "Kawasaki Barako II 175", 6, "102-YZA")
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

            val displayList = nearbyDrivers.map { "${it.name} (${it.toda})\n${it.unitModel} • ${it.tricycleType} (${it.seatCapacity}-Seater) • ${it.fare}" }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Nearby TODA Tricycle Units")
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
                        putExtra("TRICYCLE_TYPE", selectedDriver.tricycleType)
                        putExtra("UNIT_MODEL", selectedDriver.unitModel)
                        putExtra("SEAT_CAPACITY", selectedDriver.seatCapacity)
                        putExtra("PLATE_NUMBER", selectedDriver.plateNumber)
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
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
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
                    .snippet("${driver.unitModel} • ${driver.tricycleType} (${driver.seatCapacity}-Seater) • Plate: ${driver.plateNumber}")
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
              "origin": {"location": {"latLng": {"latitude": ${origin.latitude}, "longitude": ${origin.longitude}}}},
              "destination": {"location": {"latLng": {"latitude": ${destination.latitude}, "longitude": ${destination.longitude}}}},
              "travelMode": "DRIVE"
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
            if (!response.isSuccessful) return@withContext existingPolyline

            val json = JSONObject(responseBody)
            val routes = json.optJSONArray("routes") ?: return@withContext existingPolyline
            if (routes.length() == 0) return@withContext existingPolyline

            val encodedPoints = routes.getJSONObject(0).getJSONObject("polyline").getString("encodedPolyline")
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
