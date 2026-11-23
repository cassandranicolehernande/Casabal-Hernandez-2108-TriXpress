package com.example.trixpress

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale

class TrackingActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var routePolyline: Polyline? = null
    private val httpClient = OkHttpClient()

    private val defaultPickup = GeoPoint(14.07280, 120.63250)
    private val defaultDest = GeoPoint(14.07520, 120.63580)

    private lateinit var pickupLocation: GeoPoint
    private lateinit var destinationLocation: GeoPoint
    private lateinit var pickupLabel: String
    private lateinit var destLabel: String
    private lateinit var tvEstimatedTime: TextView

    private var routePoints: List<GeoPoint> = emptyList()
    private var driverMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pointIndex = 0
    private var subStep = 0
    private val totalSubStepsPerSegment = 4
    private var tripPhase = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_tracking)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        pickupLabel = intent.getStringExtra("PICKUP_LOC") ?: "J.P. Laurel St., Nasugbu"
        destLabel = intent.getStringExtra("DEST_LOC") ?: "Nasugbu, Batangas"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"
        val notes = intent.getStringExtra("DRIVER_NOTES") ?: ""

        val hasPickupCoords = intent.hasExtra("PICKUP_LAT") && intent.hasExtra("PICKUP_LNG")
        val hasDestCoords = intent.hasExtra("DEST_LAT") && intent.hasExtra("DEST_LNG")

        pickupLocation = if (hasPickupCoords) {
            GeoPoint(intent.getDoubleExtra("PICKUP_LAT", defaultPickup.latitude), intent.getDoubleExtra("PICKUP_LNG", defaultPickup.longitude))
        } else {
            defaultPickup
        }

        destinationLocation = if (hasDestCoords) {
            GeoPoint(intent.getDoubleExtra("DEST_LAT", defaultDest.latitude), intent.getDoubleExtra("DEST_LNG", defaultDest.longitude))
        } else {
            defaultDest
        }

        routePoints = listOf(pickupLocation, destinationLocation)

        mapView = findViewById(R.id.trackingMap)
        MapTileHelper.applyDefaultStreetTiles(mapView)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(pickupLocation)

        val tvTripStatusTitle = findViewById<TextView>(R.id.tvTripStatusTitle)
        val tvTrackingDriver = findViewById<TextView>(R.id.tvTrackingDriver)
        val tvTrackingInstruction = findViewById<TextView>(R.id.tvTrackingInstruction)
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime)
        val btnSos = findViewById<Button>(R.id.btnSos)
        val btnActionTrip = findViewById<Button>(R.id.btnActionTrip)
        val btnOpenGoogleMapsApp = findViewById<Button>(R.id.btnOpenGoogleMapsApp)
        val fabTrackingMapLayer = findViewById<FloatingActionButton>(R.id.fabTrackingMapLayer)

        tvTripStatusTitle.text = "🛺 toward $destLabel"
        tvTrackingDriver.text = "Driver: $driverName\nRoute: $pickupLabel ➔ $destLabel\nNote: ${notes.ifEmpty { "None" }}"
        tvEstimatedTime.text = "Calculating road route via OSRM..."

        btnOpenGoogleMapsApp.setOnClickListener {
            launchNativeGoogleMapsNavigation()
        }

        var isTopo = false
        fabTrackingMapLayer.setOnClickListener {
            isTopo = !isTopo
            MapTileHelper.toggleStreetAndTopo(mapView, isTopo)
        }

        btnSos.setOnClickListener {
            Toast.makeText(this, "Emergency SOS Alert Sent to Admin & Authorities!", Toast.LENGTH_LONG).show()
        }

        btnActionTrip.setOnClickListener {
            if (tripPhase == 1) {
                tripPhase = 2
                tvTripStatusTitle.text = "🛺 En Route to $destLabel"
                tvTrackingInstruction.text = "Heading towards $destLabel..."
                btnActionTrip.text = "Complete Trip & View Receipt"
                Toast.makeText(this, "Driver confirmed arrival! En route to destination.", Toast.LENGTH_SHORT).show()
                startRoadFollowingDriverMovement()
            } else {
                handler.removeCallbacksAndMessages(null)
                Toast.makeText(this, "Trip Completed Successfully!", Toast.LENGTH_SHORT).show()
                val nextIntent = Intent(this, ReceiptActivity::class.java).apply {
                    putExtra("DRIVER_NAME", driverName)
                    putExtra("PICKUP_LOC", pickupLabel)
                    putExtra("DEST_LOC", destLabel)
                    putExtra("EST_FARE", estFare)
                }
                startActivity(nextIntent)
                finish()
            }
        }

        updateMapMarkersAndRoute()
    }

    private fun updateMapMarkersAndRoute() {
        mapView.overlays.clear()

        val pMarker = Marker(mapView).apply {
            position = pickupLocation
            title = "Pick-up: $pickupLabel"
        }
        val dMarker = Marker(mapView).apply {
            position = destinationLocation
            title = "Drop-off: $destLabel"
        }
        driverMarker = Marker(mapView).apply {
            position = pickupLocation
            title = "🛺 Driver"
        }

        mapView.overlays.add(pMarker)
        mapView.overlays.add(dMarker)
        mapView.overlays.add(driverMarker)

        routePoints = generateInterpolatedRoadPath(pickupLocation, destinationLocation)
        redrawPolyline(routePoints)
        zoomToFitRoute(routePoints)

        lifecycleScope.launch {
            fetchAndDrawOsrmRoute(pickupLocation, destinationLocation)
        }
    }

    private fun generateInterpolatedRoadPath(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()
        val steps = 20
        for (i in 0..steps) {
            val fraction = i.toFloat() / steps.toFloat()
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction
            points.add(GeoPoint(lat, lng))
        }
        return points
    }

    private suspend fun fetchAndDrawOsrmRoute(origin: GeoPoint, destination: GeoPoint) {
        val routeResult = withContext(Dispatchers.IO) {
            val url = "https://router.project-osrm.org/route/v1/driving/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full"
            val request = Request.Builder().url(url).get().build()
            try {
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (!response.isSuccessful || body == null) return@withContext null
                    val json = JSONObject(body)
                    val routes = json.optJSONArray("routes") ?: return@withContext null
                    if (routes.length() == 0) return@withContext null
                    val route = routes.getJSONObject(0)
                    val geometry = route.getString("geometry")
                    val distance = route.optDouble("distance", -1.0)
                    val duration = route.optDouble("duration", -1.0)

                    val points = decodePolyline(geometry)
                    OsrmData(
                        points = if (points.isNotEmpty()) points else listOf(origin, destination),
                        distanceKm = if (distance >= 0) distance / 1000.0 else null,
                        durationMins = if (duration >= 0) Math.max(1, Math.ceil(duration / 60.0).toInt()) else null
                    )
                }
            } catch (e: Exception) {
                Log.e("OSRM", "Error fetching route", e)
                null
            }
        }

        if (routeResult != null && routeResult.points.size >= 2) {
            routePoints = routeResult.points
            redrawPolyline(routePoints)
            zoomToFitRoute(routePoints)

            val kmStr = routeResult.distanceKm?.let { String.format(Locale.getDefault(), "%.2f km", it) } ?: ""
            val minStr = routeResult.durationMins?.let { "$it min(s)" } ?: ""
            val summary = listOf(minStr, kmStr).filter { it.isNotEmpty() }.joinToString(" • ")
            tvEstimatedTime.text = if (summary.isNotEmpty()) "Estimated Arrival: $summary" else "Road route loaded"

            if (tripPhase == 2) {
                startRoadFollowingDriverMovement()
            }
        }
    }

    private data class OsrmData(val points: List<GeoPoint>, val distanceKm: Double?, val durationMins: Int?)

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

    private fun redrawPolyline(points: List<GeoPoint>) {
        routePolyline?.let { mapView.overlays.remove(it) }
        routePolyline = Polyline(mapView).apply {
            setPoints(points)
            color = "#1A73E8".toColorInt()
            width = 16f
        }
        mapView.overlays.add(routePolyline)
        mapView.invalidate()
    }

    private fun zoomToFitRoute(points: List<GeoPoint>) {
        if (points.isEmpty()) return
        try {
            var minLat = points[0].latitude
            var maxLat = points[0].latitude
            var minLon = points[0].longitude
            var maxLon = points[0].longitude
            for (p in points) {
                minLat = Math.min(minLat, p.latitude)
                maxLat = Math.max(maxLat, p.latitude)
                minLon = Math.min(minLon, p.longitude)
                maxLon = Math.max(maxLon, p.longitude)
            }
            val box = BoundingBox(maxLat, maxLon, minLat, minLon)
            mapView.post {
                try {
                    mapView.zoomToBoundingBox(box, true, 120)
                } catch (_: Exception) {
                    mapView.controller.setCenter(pickupLocation)
                }
            }
        } catch (_: Exception) {
            mapView.controller.setCenter(pickupLocation)
        }
    }

    private fun launchNativeGoogleMapsNavigation() {
        val destStr = "${destinationLocation.latitude},${destinationLocation.longitude}"
        val mapsUri = Uri.parse("google.navigation:q=$destStr&mode=d")
        val intent = Intent(Intent.ACTION_VIEW, mapsUri).setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destStr&travelmode=driving")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun startRoadFollowingDriverMovement() {
        handler.removeCallbacksAndMessages(null)
        pointIndex = 0
        subStep = 0
        val totalPoints = routePoints.size
        if (totalPoints < 2) return

        val runnable = object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    if (pointIndex < totalPoints - 1) {
                        val startPoint = routePoints[pointIndex]
                        val endPoint = routePoints[pointIndex + 1]
                        val fraction = subStep.toFloat() / totalSubStepsPerSegment

                        val currentLat = startPoint.latitude + (endPoint.latitude - startPoint.latitude) * fraction
                        val currentLng = startPoint.longitude + (endPoint.longitude - startPoint.longitude) * fraction
                        val newPos = GeoPoint(currentLat, currentLng)

                        driverMarker?.position = newPos
                        mapView.controller.setCenter(newPos)
                        mapView.invalidate()

                        val results = FloatArray(1)
                        Location.distanceBetween(
                            newPos.latitude, newPos.longitude,
                            destinationLocation.latitude, destinationLocation.longitude,
                            results
                        )
                        val remainingKm = results[0] / 1000f
                        val remainingMins = Math.max(1, Math.ceil(remainingKm / 0.35).toInt())

                        tvEstimatedTime.text = "Estimated Arrival: $remainingMins min(s) (${String.format(Locale.getDefault(), "%.2f", remainingKm)} km away)"

                        subStep++
                        if (subStep > totalSubStepsPerSegment) {
                            subStep = 0
                            pointIndex++
                        }
                        handler.postDelayed(this, 300)
                    } else {
                        driverMarker?.position = destinationLocation
                        tvEstimatedTime.text = "Arrived at destination!"
                        Toast.makeText(this@TrackingActivity, "You have arrived at your destination!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        handler.post(runnable)
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
