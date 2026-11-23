package com.example.trixpress

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class TricycleDriver(
    val name: String,
    val toda: String,
    val location: GeoPoint,
    val fare: String
)

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var activeRoutePolyline: Polyline? = null
    private val httpClient = OkHttpClient()

    private val passengerLocation = GeoPoint(14.0689, 120.6295)
    private val sampleDestination = GeoPoint(14.0725, 120.6335)

    private val allDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", GeoPoint(14.0667, 120.6261), "₱40.00"),
        TricycleDriver("Kuya Lito", "ARASOF Campus TODA", GeoPoint(14.0667, 120.6261), "₱40.00"),
        TricycleDriver("Kuya Cardo", "ARASOF Campus TODA", GeoPoint(14.0667, 120.6261), "₱40.00"),
        TricycleDriver("Kuya Juaning", "Town Proper TODA", GeoPoint(14.0743, 120.6325), "₱45.00"),
        TricycleDriver("Kuya Dodong", "Town Proper TODA", GeoPoint(14.0743, 120.6325), "₱45.00"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", GeoPoint(14.0951688, 120.6483039), "₱50.00"),
        TricycleDriver("Kuya Ben", "Bunducan TODA", GeoPoint(14.0951688, 120.6483039), "₱50.00"),
        TricycleDriver("Kuya Nilo", "Wawa TODA", GeoPoint(14.09945, 120.64394), "₱60.00"),
        TricycleDriver("Kuya Mark", "Natipuan Beach TODA", GeoPoint(14.1283, 120.6019), "₱80.00")
    )

    private var nearbyDrivers = listOf<TricycleDriver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_map)

        nearbyDrivers = filterDriversByDistance(
            drivers = allDrivers,
            origin = passengerLocation,
            maxDistanceMeters = 1500f
        )

        mapView = findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(passengerLocation)

        val pMarker = Marker(mapView).apply {
            position = passengerLocation
            title = "Your Pickup Location"
            snippet = "BatStateU ARASOF Campus"
        }
        mapView.overlays.add(pMarker)

        for (driver in nearbyDrivers) {
            val dMarker = Marker(mapView).apply {
                position = driver.location
                title = "${driver.name} • ${driver.toda}"
                snippet = "Fare: ${driver.fare}"
            }
            mapView.overlays.add(dMarker)
        }
        mapView.invalidate()

        lifecycleScope.launch {
            activeRoutePolyline = drawOsrmRoute(
                map = mapView,
                origin = passengerLocation,
                destination = sampleDestination,
                existingPolyline = activeRoutePolyline
            )
        }

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

            AlertDialog.Builder(this)
                .setTitle("Nearby TODA Drivers Only")
                .setItems(displayList) { _, which ->
                    val selectedDriver = nearbyDrivers[which]

                    lifecycleScope.launch {
                        activeRoutePolyline = drawOsrmRoute(
                            map = mapView,
                            origin = selectedDriver.location,
                            destination = passengerLocation,
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

    private suspend fun drawOsrmRoute(
        map: MapView,
        origin: GeoPoint,
        destination: GeoPoint,
        existingPolyline: Polyline? = null
    ): Polyline? = withContext(Dispatchers.IO) {
        val url = "https://router.project-osrm.org/route/v1/driving/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full"
        val request = Request.Builder().url(url).get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) return@withContext existingPolyline
                val json = JSONObject(body)
                val routes = json.optJSONArray("routes") ?: return@withContext existingPolyline
                if (routes.length() == 0) return@withContext existingPolyline
                val route = routes.getJSONObject(0)
                val geometry = route.getString("geometry")
                val points = decodePolyline(geometry)

                withContext(Dispatchers.Main) {
                    existingPolyline?.let { map.overlays.remove(it) }
                    val poly = Polyline(map).apply {
                        setPoints(if (points.isNotEmpty()) points else listOf(origin, destination))
                        color = "#1A237E".toColorInt()
                        width = 12f
                    }
                    map.overlays.add(poly)
                    map.invalidate()
                    poly
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            existingPolyline
        }
    }

    private fun decodePolyline(encoded: String): List<GeoPoint> {
        val poly = mutableListOf<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(GeoPoint(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }

    private fun filterDriversByDistance(
        drivers: List<TricycleDriver>,
        origin: GeoPoint,
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
