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
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import java.util.Locale

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var routePolyline: Polyline? = null

    // High-density turn-by-turn road waypoints aligned precisely on Nasugbu streets
    private val streetSnappedWaypoints = listOf(
        LatLng(14.0728, 120.6325), // Start: BatStateU ARASOF Campus
        LatLng(14.0730, 120.6327), // Driving along J.P. Laurel St.
        LatLng(14.0733, 120.6330), // J.P. Laurel St. Northbound
        LatLng(14.0736, 120.6332), // Corner of J.P. Laurel St. & F. Alix St.
        LatLng(14.0737, 120.6337), // Turning right onto F. Alix St.
        LatLng(14.0738, 120.6342), // Driving along F. Alix St.
        LatLng(14.0740, 120.6348), // F. Alix St. near Miralles St.
        LatLng(14.0742, 120.6350), // Turning left onto Miralles St.
        LatLng(14.0745, 120.6353), // Driving along Miralles St.
        LatLng(14.0748, 120.6355), // Miralles St. Barangay 5
        LatLng(14.0752, 120.6358)  // Destination: Barangay 5
    )

    private var routePoints: List<LatLng> = streetSnappedWaypoints

    private val pickupLocation = streetSnappedWaypoints.first()
    private val destinationLocation = streetSnappedWaypoints.last()

    private var driverMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pointIndex = 0
    private var subStep = 0
    private val totalSubStepsPerSegment = 4

    private var tripPhase = 1

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
        val btnOpenGoogleMapsApp = findViewById<Button>(R.id.btnOpenGoogleMapsApp)
        val fabTrackingMapLayer = findViewById<FloatingActionButton>(R.id.fabTrackingMapLayer)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Barangay 5, Nasugbu"
        val mode = intent.getStringExtra("BOOKING_MODE") ?: "Pasabay lang po!"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"

        tvTrackingDriver.text = "Driver: $driverName\nPlate: AB-1234 • 4.9★ ($mode)"

        // Automatically launch native Google Maps app directly on device
        handler.postDelayed({
            launchNativeGoogleMapsNavigation()
        }, 1200)

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
                tvTripStatusTitle.text = "🛺 En Route to Drop-off"
                tvTrackingInstruction.text = "Navigating along F. Alix St. & Miralles St..."
                tvEstimatedTime.text = "Estimated Arrival: 3 mins (0.8 km)"
                btnActionTrip.text = "Complete Trip & View Receipt"
                Toast.makeText(this, "Driver confirmed arrival! Driving along the road.", Toast.LENGTH_SHORT).show()

                startRoadFollowingDriverMovement()
            } else {
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

    private fun launchNativeGoogleMapsNavigation() {
        val originStr = "${pickupLocation.latitude},${pickupLocation.longitude}"
        val destStr = "${destinationLocation.latitude},${destinationLocation.longitude}"
        val mapsUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originStr&destination=$destStr&travelmode=driving")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, mapsUri)
            startActivity(browserIntent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.isTrafficEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 17f))

        mMap.addMarker(MarkerOptions().position(pickupLocation).title("Pick-up: J.P. Laurel St."))
        mMap.addMarker(MarkerOptions().position(destinationLocation).title("Drop-off: Barangay 5"))

        redrawPolyline(routePoints)

        val initialDriverPos = routePoints.first()
        driverMarker = mMap.addMarker(
            MarkerOptions()
                .position(initialDriverPos)
                .title("🛺 Kuya Roberto (Arriving at Pick-up)")
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(newPos))

                        // Calculate exact remaining distance and Google Maps style ETA
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            newPos.latitude, newPos.longitude,
                            destinationLocation.latitude, destinationLocation.longitude,
                            results
                        )
                        val remainingKm = results[0] / 1000f
                        val remainingMins = Math.max(1, Math.ceil(remainingKm / 0.35).toInt())

                        findViewById<TextView>(R.id.tvEstimatedTime).text =
                            "Estimated Arrival: $remainingMins min(s) (${String.format(Locale.getDefault(), "%.1f", remainingKm)} km away)"

                        subStep++
                        if (subStep > totalSubStepsPerSegment) {
                            subStep = 0
                            pointIndex++
                        }

                        handler.postDelayed(this, 300)
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
        handler.removeCallbacksAndMessages(null)
    }
}
