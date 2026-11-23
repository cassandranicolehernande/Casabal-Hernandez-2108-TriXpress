package com.example.trixpress

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import java.util.Locale

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var routePolyline: Polyline? = null

    private lateinit var pickupLocation: LatLng
    private lateinit var destinationLocation: LatLng
    private var routePoints: List<LatLng> = emptyList()

    private var driverMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pointIndex = 0
    private var subStep = 0
    private val totalSubStepsPerSegment = 4
    private var tripPhase = 1

    private lateinit var pickupLoc: String
    private lateinit var destLoc: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "Current Live GPS Location"
        destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"
        val notes = intent.getStringExtra("DRIVER_NOTES") ?: ""

        val pLat = intent.getDoubleExtra("PICKUP_LAT", 14.07280)
        val pLng = intent.getDoubleExtra("PICKUP_LNG", 120.63250)
        val dLat = intent.getDoubleExtra("DEST_LAT", 14.07520)
        val dLng = intent.getDoubleExtra("DEST_LNG", 120.63580)

        pickupLocation = LatLng(pLat, pLng)
        destinationLocation = LatLng(dLat, dLng)
        routePoints = generateDynamicRoutePath(pickupLocation, destinationLocation)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.trackingMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val tvTripStatusTitle = findViewById<TextView>(R.id.tvTripStatusTitle)
        val tvTrackingDriver = findViewById<TextView>(R.id.tvTrackingDriver)
        val tvTrackingInstruction = findViewById<TextView>(R.id.tvTrackingInstruction)
        val tvEstimatedTime = findViewById<TextView>(R.id.tvEstimatedTime)
        val btnSos = findViewById<Button>(R.id.btnSos)
        val btnActionTrip = findViewById<Button>(R.id.btnActionTrip)
        val btnOpenGoogleMapsApp = findViewById<Button>(R.id.btnOpenGoogleMapsApp)
        val fabTrackingMapLayer = findViewById<FloatingActionButton>(R.id.fabTrackingMapLayer)

        tvTripStatusTitle.text = "🛺 Driver En Route to Pick-up"
        tvTrackingDriver.text = "Driver: $driverName\nRoute: $pickupLoc ➔ $destLoc\nNote: ${notes.ifEmpty { "None" }}"
        tvEstimatedTime.text = "Estimated Arrival: 3 mins"

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
                tvTripStatusTitle.text = "🛺 En Route to $destLoc"
                tvTrackingInstruction.text = "Navigating from $pickupLoc to $destLoc..."
                tvEstimatedTime.text = "Estimated Arrival: 4 mins"
                btnActionTrip.text = "Complete Trip & View Receipt"
                Toast.makeText(this, "Driver confirmed arrival! En route to destination.", Toast.LENGTH_SHORT).show()
                startRoadFollowingDriverMovement()
            } else {
                handler.removeCallbacksAndMessages(null)
                Toast.makeText(this, "Trip Completed Successfully!", Toast.LENGTH_SHORT).show()
                val nextIntent = Intent(this, ReceiptActivity::class.java).apply {
                    putExtra("DRIVER_NAME", driverName)
                    putExtra("PICKUP_LOC", pickupLoc)
                    putExtra("DEST_LOC", destLoc)
                    putExtra("EST_FARE", estFare)
                }
                startActivity(nextIntent)
                finish()
            }
        }
    }

    private fun generateDynamicRoutePath(start: LatLng, end: LatLng): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val steps = 25
        for (i in 0..steps) {
            val fraction = i.toFloat() / steps.toFloat()
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction
            points.add(LatLng(lat, lng))
        }
        return points
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.isTrafficEnabled = true

        mMap.addMarker(MarkerOptions().position(pickupLocation).title("Pick-up: $pickupLoc"))
        mMap.addMarker(MarkerOptions().position(destinationLocation).title("Drop-off: $destLoc"))

        routePoints = generateDynamicRoutePath(pickupLocation, destinationLocation)
        redrawPolyline(routePoints)
        zoomToFitRoute(routePoints)

        val initialDriverPos = routePoints.first()
        driverMarker = mMap.addMarker(
            MarkerOptions()
                .position(initialDriverPos)
                .title("🛺 Kuya Roberto (Active Driver)")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    private fun redrawPolyline(points: List<LatLng>) {
        routePolyline?.remove()
        routePolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(18f)
                .color("#1A73E8".toColorInt())
                .startCap(RoundCap())
                .endCap(RoundCap())
                .jointType(JointType.ROUND)
        )
    }

    private fun zoomToFitRoute(points: List<LatLng>) {
        if (!::mMap.isInitialized || points.isEmpty()) return
        try {
            val builder = LatLngBounds.Builder()
            for (p in points) builder.include(p)
            val bounds = builder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 140))
        } catch (e: Exception) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 16f))
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
                if (!isFinishing && ::mMap.isInitialized) {
                    if (pointIndex < totalPoints - 1) {
                        val startPoint = routePoints[pointIndex]
                        val endPoint = routePoints[pointIndex + 1]
                        val fraction = subStep.toFloat() / totalSubStepsPerSegment

                        val currentLat = startPoint.latitude + (endPoint.latitude - startPoint.latitude) * fraction
                        val currentLng = startPoint.longitude + (endPoint.longitude - startPoint.longitude) * fraction
                        val newPos = LatLng(currentLat, currentLng)

                        driverMarker?.position = newPos

                        val results = FloatArray(1)
                        Location.distanceBetween(
                            newPos.latitude, newPos.longitude,
                            destinationLocation.latitude, destinationLocation.longitude,
                            results
                        )
                        val remainingKm = results[0] / 1000f
                        val remainingMins = Math.max(1, Math.ceil(remainingKm / 0.30).toInt())

                        findViewById<TextView>(R.id.tvEstimatedTime).text =
                            "Estimated Arrival: $remainingMins min(s) (${String.format(Locale.getDefault(), "%.2f", remainingKm * 1000)}m away)"

                        subStep++
                        if (subStep > totalSubStepsPerSegment) {
                            subStep = 0
                            pointIndex++
                        }
                        handler.postDelayed(this, 300)
                    } else {
                        driverMarker?.position = destinationLocation
                        findViewById<TextView>(R.id.tvEstimatedTime).text = "Arrived at destination!"
                        Toast.makeText(this@TrackingActivity, "You have arrived at your destination!", Toast.LENGTH_LONG).show()
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
}
