package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // Mock route locations: ARASOF Campus to Public Market
    private val pickupLocation = LatLng(14.0689, 120.6295)
    private val destinationLocation = LatLng(14.0722, 120.6341)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.trackingMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val tvTrackingDriver = findViewById<TextView?>(R.id.tvTrackingDriver)
        val tvEstimatedTime = findViewById<TextView?>(R.id.tvEstimatedTime)
        val btnSos = findViewById<Button?>(R.id.btnSos)
        val btnCompleteTrip = findViewById<Button?>(R.id.btnCompleteTrip)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Mario (Bunducan TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"

        tvTrackingDriver?.text = "Driver: $driverName"
        tvEstimatedTime?.text = "Estimated Arrival: 10 mins"

        btnSos?.setOnClickListener {
            Toast.makeText(this, "Emergency Alert Sent to Local TODA & Emergency Contacts!", Toast.LENGTH_LONG).show()
        }

        btnCompleteTrip?.setOnClickListener {
            Toast.makeText(this, "Trip Completed!", Toast.LENGTH_SHORT).show()

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15f))

        mMap.addMarker(
            MarkerOptions()
                .position(pickupLocation)
                .title("Pickup: BatStateU ARASOF Campus")
        )

        mMap.addMarker(
            MarkerOptions()
                .position(destinationLocation)
                .title("Destination: Nasugbu Public Market")
        )
    }
}
