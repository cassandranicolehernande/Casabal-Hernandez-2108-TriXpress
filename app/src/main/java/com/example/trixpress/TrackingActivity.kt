package com.example.trixpress

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var routePolyline: Polyline? = null
    private val httpClient = OkHttpClient()

    private val defaultPickup = LatLng(14.07280, 120.63250) // J.P. Laurel St., Nasugbu
    private val defaultDest = LatLng(14.07520, 120.63580)   // Nasugbu area fallback

    private lateinit var pickupLocation: LatLng
    private lateinit var destinationLocation: LatLng
    private lateinit var pickupLabel: String
    private lateinit var destLabel: String
    private lateinit var tvEstimatedTime: TextView

    private var routePoints: List<LatLng> = emptyList()
    private var driverMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pointIndex = 0
    private var subStep = 0
    private val totalSubStepsPerSegment = 4
    private var tripPhase = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        pickupLabel = intent.getStringExtra("PICKUP_LOC") ?: "J.P. Laurel St., Nasugbu"
        destLabel = intent.getStringExtra("DEST_LOC") ?: "Nasugbu, Batangas"
        val mode = intent.getStringExtra("BOOKING_MODE") ?: "Pasabay lang po!"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"
        val notes = intent.getStringExtra("DRIVER_NOTES") ?: ""

        val hasPickupCoords = intent.hasExtra("PICKUP_LAT") && intent.hasExtra("PICKUP_LNG")
        val hasDestCoords = intent.hasExtra("DEST_LAT") && intent.hasExtra("DEST_LNG")

        pickupLocation = if (hasPickupCoords) {
            LatLng(
                intent.getDoubleExtra("PICKUP_LAT", defaultPickup.latitude),
                intent.getDoubleExtra("PICKUP_LNG", defaultPickup.longitude)
            )
        } else {
            defaultPickup
        }

        destinationLocation = if (hasDestCoords) {
            LatLng(
                intent.getDoubleExtra("DEST_LAT", defaultDest.latitude),
                intent.getDoubleExtra("DEST_LNG", defaultDest.longitude)
            )
        } else {
            defaultDest
        }

        routePoints = listOf(pickupLocation, destinationLocation)

        val tvTripStatusTitle = findViewById<TextView>(R.id.tvTripStatusTitle)
        val tvTrackingDriver = findViewById<TextView>(R.id.tvTrackingDriver)
        val tvTrackingInstruction = findViewById<TextView>(R.id.tvTrackingInstruction)
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime)
        val btnSos = findViewById<Button>(R.id.btnSos)
        val btnActionTrip = findViewById<Button>(R.id.btnActionTrip)
        val btnOpenGoogleMapsApp = findViewById<Button>(R.id.btnOpenGoogleMapsApp)
        val fabTrackingMapLayer = findViewById<FloatingActionButton>(R.id.fabTrackingMapLayer)

        tvTripStatusTitle.text = "🛺 toward $destLabel"
        tvTrackingDriver.text = "Driver: $driverName\nRoute: $pickupLabel ➔ $destLabel\nMode: $mode • Note: ${notes.ifEmpty { "None" }}"
        tvEstimatedTime.text = "Calculating road route & ETA..."

        btnOpenGoogleMapsApp.setOnClickListener {
            launchNativeGoogleMapsNavigation()
        }

        fabTrackingMapLayer.setOnClickListener {
            if (::mMap.isInitialized) {
                if (mMap.mapType == GoogleMap.MAP_TYPE_HYBRID) {
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    Toast.makeText(this, "Map View: Standard", Toast.LENGTH_SHORT).show()
                } else {
                    mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    Toast.makeText(this, "Map View: Satellite Hybrid", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnSos.setOnClickListener {
            Toast.makeText(this, "Emergency SOS Alert Sent to Admin & Authorities!", Toast.LENGTH_LONG).show()
        }

        btnActionTrip.setOnClickListener {
            if (tripPhase == 1) {
                tripPhase = 2
                tvTripStatusTitle.text = "🛺 En Route to $destLabel"
                tvTrackingInstruction.text = "Navigating along road to $destLabel..."
                btnActionTrip.text = "Complete Trip & View Receipt"
                Toast.makeText(this, "Driver confirmed arrival! Navigating along road.", Toast.LENGTH_SHORT).show()

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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.trackingMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!hasPickupCoords || !hasDestCoords) {
            lifecycleScope.launch {
                val resolvedPickup = if (!hasPickupCoords) {
                    geocodePlaceName(pickupLabel) ?: defaultPickup
                } else {
                    pickupLocation
                }
                val resolvedDest = if (!hasDestCoords) {
                    geocodePlaceName(destLabel) ?: defaultDest
                } else {
                    destinationLocation
                }
                pickupLocation = resolvedPickup
                destinationLocation = resolvedDest
                updateMapMarkersAndRoute()
            }
        }
    }

    private suspend fun geocodePlaceName(placeName: String): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(this@TrackingActivity, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName("$placeName, Nasugbu, Batangas, Philippines", 1)
                val first = addresses?.firstOrNull()
                if (first != null) LatLng(first.latitude, first.longitude) else null
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun launchNativeGoogleMapsNavigation() {
        val originStr = "${pickupLocation.latitude},${pickupLocation.longitude}"
        val destStr = "${destinationLocation.latitude},${destinationLocation.longitude}"
        val mapsUri = Uri.parse("google.navigation:q=$destStr&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(mapIntent)
        } catch (_: Exception) {
            val webUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&origin=$originStr&destination=$destStr&travelmode=driving"
            )
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.isTrafficEnabled = true
        updateMapMarkersAndRoute()
    }

    private fun updateMapMarkersAndRoute() {
        if (!::mMap.isInitialized) return
        mMap.clear()

        mMap.addMarker(MarkerOptions().position(pickupLocation).title("Pick-up: $pickupLabel"))
        mMap.addMarker(MarkerOptions().position(destinationLocation).title("Drop-off: $destLabel"))

        driverMarker = mMap.addMarker(
            MarkerOptions()
                .position(pickupLocation)
                .title("🛺 Driver")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        routePoints = listOf(pickupLocation, destinationLocation)
        redrawPolyline(routePoints)
        zoomToFitRoute(routePoints)

        lifecycleScope.launch {
            fetchAndDrawRoadRoute(pickupLocation, destinationLocation)
        }
    }

    private suspend fun fetchAndDrawRoadRoute(origin: LatLng, destination: LatLng) {
        val routeResult = withContext(Dispatchers.IO) {
            val url =
                "https://router.project-osrm.org/route/v1/driving/" +
                        "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}" +
                        "?overview=full&geometries=polyline"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        Log.e("OSRM", "HTTP ${response.code}: $responseBody")
                        return@withContext null
                    }

                    val json = JSONObject(responseBody)
                    val routes = json.optJSONArray("routes") ?: return@withContext null
                    if (routes.length() == 0) return@withContext null

                    val firstRoute = routes.getJSONObject(0)
                    val encodedPolyline = firstRoute.getString("geometry")
                    val distanceMeters = firstRoute.optDouble("distance", -1.0)
                    val durationSeconds = firstRoute.optDouble("duration", -1.0)

                    RouteData(
                        points = PolyUtil.decode(encodedPolyline),
                        distanceKm = if (distanceMeters >= 0) distanceMeters / 1000.0 else null,
                        durationMins = if (durationSeconds >= 0) {
                            Math.max(1, Math.ceil(durationSeconds / 60.0).toInt())
                        } else null
                    )
                }
            } catch (e: Exception) {
                Log.e("OSRM", "Failed to compute route", e)
                null
            }
        }

        if (routeResult != null && routeResult.points.size >= 2) {
            routePoints = routeResult.points
            redrawPolyline(routePoints)
            zoomToFitRoute(routePoints)

            val kmStr = routeResult.distanceKm?.let {
                String.format(Locale.getDefault(), "%.2f km", it)
            } ?: ""
            val minStr = routeResult.durationMins?.let { "$it min(s)" } ?: ""
            val summary = listOf(minStr, kmStr).filter { it.isNotEmpty() }.joinToString(" • ")
            tvEstimatedTime.text = if (summary.isNotEmpty()) {
                "Estimated Arrival: $summary"
            } else {
                "Road route loaded"
            }

            if (tripPhase == 2) {
                startRoadFollowingDriverMovement()
            }
        } else {
            updateFallbackDistanceAndEta(origin, destination)
        }
    }

    private fun updateFallbackDistanceAndEta(origin: LatLng, destination: LatLng) {
        val results = FloatArray(1)
        Location.distanceBetween(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude,
            results
        )
        val km = results[0] / 1000f
        val mins = Math.max(1, Math.ceil((km / 0.35).toDouble()).toInt())
        tvEstimatedTime.text =
            "Estimated Arrival: ~$mins min(s) (${String.format(Locale.getDefault(), "%.2f", km)} km)"
    }

    private fun zoomToFitRoute(points: List<LatLng>) {
        if (!::mMap.isInitialized || points.isEmpty()) return
        val builder = LatLngBounds.Builder()
        for (point in points) {
            builder.include(point)
        }
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120))
        } catch (_: Exception) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 15f))
        }
    }

    private fun redrawPolyline(points: List<LatLng>) {
        if (!::mMap.isInitialized || points.isEmpty()) return
        routePolyline?.remove()
        routePolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(16f)
                .color("#1A73E8".toColorInt())
                .startCap(RoundCap())
                .endCap(RoundCap())
                .jointType(JointType.ROUND)
        )
    }

    private fun startRoadFollowingDriverMovement() {
        handler.removeCallbacksAndMessages(null)
        pointIndex = 0
        subStep = 0

        val totalPoints = routePoints.size
        if (totalPoints < 2) return

        val runnable = object : Runnable {
            override fun run() {
                if (!isFinishing && ::mMap.isInitialized) {
                    if (pointIndex < totalPoints - 1) {
                        val startPoint = routePoints[pointIndex]
                        val endPoint = routePoints[pointIndex + 1]
                        val fraction = subStep.toFloat() / totalSubStepsPerSegment

                        val lat = startPoint.latitude + (endPoint.latitude - startPoint.latitude) * fraction
                        val lng = startPoint.longitude + (endPoint.longitude - startPoint.longitude) * fraction
                        val currentPos = LatLng(lat, lng)

                        driverMarker?.position = currentPos
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPos))

                        val results = FloatArray(1)
                        Location.distanceBetween(
                            currentPos.latitude, currentPos.longitude,
                            destinationLocation.latitude, destinationLocation.longitude,
                            results
                        )
                        val remainingKm = results[0] / 1000f
                        val remainingMins = Math.max(1, Math.ceil((remainingKm / 0.35).toDouble()).toInt())
                        tvEstimatedTime.text =
                            "Estimated Arrival: $remainingMins min(s) (${String.format(Locale.getDefault(), "%.2f", remainingKm)} km left)"

                        subStep++
                        if (subStep > totalSubStepsPerSegment) {
                            subStep = 0
                            pointIndex++
                        }
                        handler.postDelayed(this, 300)
                    } else {
                        driverMarker?.position = destinationLocation
                        tvEstimatedTime.text = "Arrived at drop-off destination!"
                        Toast.makeText(
                            this@TrackingActivity,
                            "You have arrived at your destination!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private data class RouteData(
        val points: List<LatLng>,
        val distanceKm: Double?,
        val durationMins: Int?
    )
}