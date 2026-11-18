package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // Realistic Nasugbu road waypoints (following J.P. Laurel St., Alix St., R. Martinez St.)
    private val roadWaypoints = listOf(
        LatLng(14.0689, 120.6295), // Start / Pick-up (Near Barangay 10)
        LatLng(14.0694, 120.6302), // Moving towards J.P. Laurel St.
        LatLng(14.0702, 120.6314), // Cruising along J.P. Laurel St.
        LatLng(14.0712, 120.6322), // Turning onto Alix St.
        LatLng(14.0722, 120.6341)  // Destination (Barangay 4)
    )

    private val pickupLocation = roadWaypoints.first()
    private val destinationLocation = roadWaypoints.last()

    private var driverMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var waypointIndex = 0
    private var subStep = 0
    private val totalSubStepsPerSegment = 5

    private var tripPhase = 1 // 1 = Arriving at Pickup, 2 = En Route to Drop-off

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.trackingMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val tvTripStatusTitle = findViewById<TextView>(R.id.tvTripStatusTitle)
        val tvTrackingDriver = findViewById<TextView>(R.id.tvTrackingDriver)
        val tvTrackingInstruction = findViewById<TextView>(R.id.tvTrackingInstruction)
        val tvEstimatedTime = findViewById<TextView>(R.id.tvEstimatedTime)
        val btnSos = findViewById<Button>(R.id.btnSos)
        val btnActionTrip = findViewById<Button>(R.id.btnActionTrip)
        val fabTrackingMapLayer = findViewById<FloatingActionButton>(R.id.fabTrackingMapLayer)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val mode = intent.getStringExtra("BOOKING_MODE") ?: "Pasabay lang po!"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"

        tvTrackingDriver.text = "Driver: $driverName\nPlate: AB-1234 • 4.9★ ($mode)"

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
                // Phase 1 -> Phase 2: Driver arrived, start trip following road waypoints
                tripPhase = 2
                tvTripStatusTitle.text = "🛺 En Route to Drop-off"
                tvTrackingInstruction.text = "Driving along J.P. Laurel St. & Alix St..."
                tvEstimatedTime.text = "Estimated Arrival: 4 mins"
                btnActionTrip.text = "Complete Trip & View Receipt"
                Toast.makeText(this, "Driver confirmed arrival! Driving along the road.", Toast.LENGTH_SHORT).show()

                // Start road-following live movement simulation
                startRoadFollowingDriverMovement()
            } else {
                // Trip complete -> Receipt
                handler.removeCallbacksAndMessages(null)
                Toast.makeText(this, "Trip Completed Successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ReceiptActivity::class.java).apply {
                    putExtra("DRIVER_NAME", driverName)
                    putExtra("PICKUP_LOC", pickupLoc)
                    putExtra("DEST_LOC", destLoc)
                    putExtra("EST_FARE", estFare)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.isTrafficEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 17f))

        mMap.addMarker(MarkerOptions().position(pickupLocation).title("Pick-up Location"))
        mMap.addMarker(MarkerOptions().position(destinationLocation).title("Drop-off Destination"))

        // Draw Google Maps Route Polyline precisely following Nasugbu road waypoints
        mMap.addPolyline(
            PolylineOptions()
                .addAll(roadWaypoints)
                .width(14f)
                .color(android.graphics.Color.parseColor("#4AA6EB")) // Navigation blue route on roads
        )

        val initialDriverPos = roadWaypoints[0]
        driverMarker = mMap.addMarker(
            MarkerOptions()
                .position(initialDriverPos)
                .title("🛺 Kuya Roberto (Arriving at Pick-up)")
        )
    }

    private fun startRoadFollowingDriverMovement() {
        waypointIndex = 0
        subStep = 0

        val runnable = object : Runnable {
            override fun run() {
                // Ensure activity and map are still valid before updating UI
                if (!isFinishing && ::mMap.isInitialized) {
                    if (waypointIndex < roadWaypoints.size - 1) {
                        val startPoint = roadWaypoints[waypointIndex]
                        val endPoint = roadWaypoints[waypointIndex + 1]

                        val currentLat = startPoint.latitude + (endPoint.latitude - startPoint.latitude) * (subStep.toFloat() / totalSubStepsPerSegment)
                        val currentLng = startPoint.longitude + (endPoint.longitude - startPoint.longitude) * (subStep.toFloat() / totalSubStepsPerSegment)
                        val newPos = LatLng(currentLat, currentLng)

                        driverMarker?.position = newPos
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(newPos))

                        val remainingMins = Math.max(1, 4 - waypointIndex)
                        findViewById<TextView>(R.id.tvEstimatedTime).text = "Estimated Arrival: $remainingMins min(s) away"

                        subStep++
                        if (subStep > totalSubStepsPerSegment) {
                            subStep = 0
                            waypointIndex++
                        }

                        handler.postDelayed(this, 2000)
                    } else {
                        driverMarker?.position = destinationLocation
                        findViewById<TextView>(R.id.tvEstimatedTime).text = "Arrived at drop-off destination!"
                        Toast.makeText(this@TrackingActivity, "You have arrived at your destination!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Prevent memory leaks
    }
}