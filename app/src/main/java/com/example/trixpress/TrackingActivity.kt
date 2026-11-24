package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private var isDriver = false

    private var pointIndex = 0
    private var subStep = 0
    private val totalSubStepsPerSegment = 4

    private lateinit var pickupLoc: String
    private lateinit var destLoc: String
    private lateinit var driverName: String
    private lateinit var estFare: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        isDriver = intent.getBooleanExtra("IS_DRIVER", false)
        driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF - Nasugbu Campus"
        destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"
        val notes = intent.getStringExtra("DRIVER_NOTES") ?: ""

        val hasPickupCoords = intent.hasExtra("PICKUP_LAT") && intent.getDoubleExtra("PICKUP_LAT", 0.0) != 0.0
        val hasDestCoords = intent.hasExtra("DEST_LAT") && intent.getDoubleExtra("DEST_LAT", 0.0) != 0.0

        val resolvedPickupLatLng = if (hasPickupCoords) {
            LatLng(intent.getDoubleExtra("PICKUP_LAT", 14.0673), intent.getDoubleExtra("PICKUP_LNG", 120.6262))
        } else {
            NasugbuLocations.findCoordinatesByName(pickupLoc)
        }

        val resolvedDestLatLng = if (hasDestCoords) {
            LatLng(intent.getDoubleExtra("DEST_LAT", 14.0754), intent.getDoubleExtra("DEST_LNG", 120.6358))
        } else {
            NasugbuLocations.findCoordinatesByName(destLoc)
        }

        pickupLocation = resolvedPickupLatLng
        destinationLocation = resolvedDestLatLng
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

        val layoutPassengerTrackingControls = findViewById<View>(R.id.layoutPassengerTrackingControls)
        val btnContinueTracking = findViewById<Button>(R.id.btnContinueTracking)
        val btnCancelTracking = findViewById<Button>(R.id.btnCancelTracking)

        if (isDriver) {
            tvTripStatusTitle.text = "🛺 Driver Dispatch: Passenger Locations"
            tvTrackingDriver.text = "Passenger Request\nPick-up: $pickupLoc\nDrop-off: $destLoc"
            tvTrackingInstruction.text = "Exact locations given by passenger. Tap Google Maps App for turn-by-turn navigation."
            tvEstimatedTime.text = "Fare: $estFare • Note: ${notes.ifEmpty { "None" }}"
            btnActionTrip.text = "Complete Trip & Save to History"
            btnActionTrip.visibility = View.VISIBLE
            layoutPassengerTrackingControls.visibility = View.GONE
        } else {
            tvTripStatusTitle.text = "🛺 En Route to Destination"
            tvTrackingDriver.text = "Driver: $driverName\nRoute: $pickupLoc ➔ $destLoc\nNote: ${notes.ifEmpty { "None" }}"
            tvEstimatedTime.text = "Estimated Arrival: 3 mins"
            btnActionTrip.visibility = View.GONE
            layoutPassengerTrackingControls.visibility = View.VISIBLE
        }

        btnContinueTracking.setOnClickListener {
            Toast.makeText(this, "Active trip continuing...", Toast.LENGTH_SHORT).show()
        }

        btnCancelTracking.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancel Booking?")
                .setMessage("Are you sure you want to cancel this active booking with $driverName?")
                .setPositiveButton("Yes, Cancel Booking") { _, _ ->
                    val activePrefs = getSharedPreferences("TriXpressActiveBooking", Context.MODE_PRIVATE)
                    activePrefs.edit().clear().apply()

                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PassengerDashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Keep Ride") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

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
            handler.removeCallbacksAndMessages(null)
            Toast.makeText(this, "Trip Completed Successfully!", Toast.LENGTH_SHORT).show()
            val nextIntent = Intent(this, ReceiptActivity::class.java).apply {
                putExtra("DRIVER_NAME", driverName)
                putExtra("PICKUP_LOC", pickupLoc)
                putExtra("DEST_LOC", destLoc)
                putExtra("EST_FARE", estFare)
                putExtra("IS_DRIVER", isDriver)
            }
            startActivity(nextIntent)
            finish()
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

        if (!isDriver) {
            val initialDriverPos = routePoints.first()
            driverMarker = mMap.addMarker(
                MarkerOptions()
                    .position(initialDriverPos)
                    .title("🛺 $driverName (Active Driver)")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            startRoadFollowingDriverMovement()
        }
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

                        // Automatically transition passenger to Receipt / Rating screen upon arrival
                        handler.postDelayed({
                            if (!isFinishing) {
                                val nextIntent = Intent(this@TrackingActivity, ReceiptActivity::class.java).apply {
                                    putExtra("DRIVER_NAME", driverName)
                                    putExtra("PICKUP_LOC", pickupLoc)
                                    putExtra("DEST_LOC", destLoc)
                                    putExtra("EST_FARE", estFare)
                                    putExtra("IS_DRIVER", isDriver)
                                }
                                startActivity(nextIntent)
                                finish()
                            }
                        }, 1500)
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
