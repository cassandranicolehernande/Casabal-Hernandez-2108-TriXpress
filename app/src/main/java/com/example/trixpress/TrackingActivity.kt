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

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val pickupLocation = LatLng(14.0689, 120.6295)
    private val destinationLocation = LatLng(14.0722, 120.6341)

    private var driverMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var simulationStep = 0

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
                // Phase 1 -> Phase 2: Driver arrived, start trip to dropoff
                tripPhase = 2
                tvTripStatusTitle.text = "🛺 En Route to Drop-off"
                tvTrackingInstruction.text = "Heading to destination..."
                tvEstimatedTime.text = "Estimated Arrival: 5 mins"
                btnActionTrip.text = "Complete Trip & View Receipt"
                Toast.makeText(this, "Driver confirmed arrival! Trip started.", Toast.LENGTH_SHORT).show()

                // Start live moving simulation towards destination
                startLiveDriverMovement()
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 16f))

        mMap.addMarker(MarkerOptions().position(pickupLocation).title("Pick-up Location"))
        mMap.addMarker(MarkerOptions().position(destinationLocation).title("Drop-off Destination"))

        val initialDriverPos = LatLng(14.0685, 120.6290)
        driverMarker = mMap.addMarker(
            MarkerOptions()
                .position(initialDriverPos)
                .title("🛺 Kuya Roberto (Arriving at Pick-up)")
        )
    }

    private fun startLiveDriverMovement() {
        val latStep = (destinationLocation.latitude - pickupLocation.latitude) / 10.0
        val lngStep = (destinationLocation.longitude - pickupLocation.longitude) / 10.0
        simulationStep = 0

        val runnable = object : Runnable {
            override fun run() {
                if (simulationStep <= 10) {
                    val currentLat = pickupLocation.latitude + (latStep * simulationStep)
                    val currentLng = pickupLocation.longitude + (lngStep * simulationStep)
                    val newPos = LatLng(currentLat, currentLng)

                    driverMarker?.position = newPos
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(newPos))

                    val remainingMins = Math.max(1, 5 - (simulationStep / 2))
                    findViewById<TextView>(R.id.tvEstimatedTime).text = "Estimated Arrival: $remainingMins min(s) away"

                    simulationStep++
                    handler.postDelayed(this, 3000)
                } else {
                    findViewById<TextView>(R.id.tvEstimatedTime).text = "Arrived at drop-off destination!"
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
