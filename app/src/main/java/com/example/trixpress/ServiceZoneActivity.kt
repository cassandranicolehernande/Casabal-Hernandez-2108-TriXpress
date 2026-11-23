package com.example.trixpress

import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class ServiceZoneActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val nasugbuCenter = GeoPoint(14.0689, 120.6295)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_service_zone)

        mapView = findViewById(R.id.adminZoneMap)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
        mapView.controller.setCenter(nasugbuCenter)

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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
