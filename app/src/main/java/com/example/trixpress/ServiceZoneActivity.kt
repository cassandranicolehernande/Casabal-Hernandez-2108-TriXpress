package com.example.trixpress

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class ServiceZoneActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val nasugbuCenter = LatLng(14.0689, 120.6295)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_zone)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.adminZoneMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val switchZonesBoundary = findViewById<SwitchCompat?>(R.id.switchZonesBoundary)
        val switchActiveAvailability = findViewById<SwitchCompat?>(R.id.switchActiveAvailability)
        val switchZoneAvailability = findViewById<SwitchCompat?>(R.id.switchZoneAvailability)
        val btnBackFromZones = findViewById<Button?>(R.id.btnBackFromZones)

        switchZonesBoundary?.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Zone Boundaries $status", Toast.LENGTH_SHORT).show()
        }

        switchActiveAvailability?.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "active" else "inactive"
            Toast.makeText(this, "Active Availability $status", Toast.LENGTH_SHORT).show()
        }

        switchZoneAvailability?.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "open" else "restricted"
            Toast.makeText(this, "Zone status set to $status", Toast.LENGTH_SHORT).show()
        }

        btnBackFromZones?.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nasugbuCenter, 14f))
    }
}
