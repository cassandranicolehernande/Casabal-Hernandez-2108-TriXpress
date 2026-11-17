package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView

class PassengerDashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var drawerLayout: DrawerLayout
    private val passengerLocation = LatLng(14.0689, 120.6295)

    private val nearbyDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", LatLng(14.0692, 120.6298), "₱40.00"),
        TricycleDriver("Kuya Lito", "ARASOF Campus TODA", LatLng(14.0695, 120.6302), "₱40.00"),
        TricycleDriver("Kuya Juaning", "Town Proper TODA", LatLng(14.0722, 120.6341), "₱45.00"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", LatLng(14.0757, 120.6318), "₱50.00")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_dashboard)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnOpenDrawer = findViewById<ImageView>(R.id.btnOpenDrawer)

        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_history) {
                startActivity(Intent(this, TripHistoryActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val cardTopSearch = findViewById<MaterialCardView>(R.id.cardTopSearch)
        val btnChoosePickup = findViewById<Button>(R.id.btnChoosePickup)
        val fabMapLayer = findViewById<FloatingActionButton>(R.id.fabMapLayer)

        val openSearchIntent = {
            val intent = Intent(this, SearchDropoffActivity::class.java)
            intent.putExtra("PICKUP_LOCATION", "BatStateU ARASOF Campus, Nasugbu")
            startActivity(intent)
        }

        cardTopSearch.setOnClickListener { openSearchIntent() }
        btnChoosePickup.setOnClickListener { openSearchIntent() }

        fabMapLayer.setOnClickListener {
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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Set professional Hybrid Satellite view by default (matching Angkas/Google Maps reference)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 16f))

        // Add Passenger Marker
        mMap.addMarker(
            MarkerOptions()
                .position(passengerLocation)
                .title("Your Location")
                .snippet("BatStateU ARASOF Campus")
        )

        // Add Nearby Driver Markers
        for (driver in nearbyDrivers) {
            mMap.addMarker(
                MarkerOptions()
                    .position(driver.location)
                    .title("${driver.name} (${driver.toda})")
                    .snippet("Fare: ${driver.fare} • Rating: 4.9★")
            )
        }
    }
}
