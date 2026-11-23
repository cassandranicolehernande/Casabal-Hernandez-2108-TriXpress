package com.example.trixpress

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.LocationServices
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
    private val passengerLocation = LatLng(14.0728, 120.6325) // BatStateU ARASOF Campus, Nasugbu

    // Comprehensive Active TODA Drivers in Nasugbu
    private val nearbyDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", LatLng(14.0730, 120.6328), "₱40.00"),
        TricycleDriver("Kuya Lito", "ARASOF Campus TODA", LatLng(14.0735, 120.6332), "₱40.00"),
        TricycleDriver("Kuya Cardo", "ARASOF Campus TODA", LatLng(14.0722, 120.6318), "₱40.00"),
        TricycleDriver("Kuya Juaning", "Town Proper TODA", LatLng(14.0740, 120.6345), "₱45.00"),
        TricycleDriver("Kuya Dodong", "Town Proper TODA", LatLng(14.0745, 120.6350), "₱45.00"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", LatLng(14.0755, 120.6360), "₱50.00"),
        TricycleDriver("Kuya Ben", "Bunducan TODA", LatLng(14.0760, 120.6365), "₱50.00"),
        TricycleDriver("Kuya Nilo", "Wawa Port TODA", LatLng(14.0780, 120.6380), "₱55.00"),
        TricycleDriver("Kuya Mark", "Natipuan TODA", LatLng(14.0820, 120.6400), "₱60.00"),
        TricycleDriver("Kuya Jun", "Calayo Beach TODA", LatLng(14.0880, 120.6420), "₱65.00")
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
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("IS_DRIVER", false)
                    }
                    startActivity(intent)
                }
                R.id.nav_zones -> {
                    val intent = Intent(this, ServiceZoneActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, TripHistoryActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val cardTopSearch = findViewById<MaterialCardView>(R.id.cardTopSearch)
        val btnChoosePickup = findViewById<Button>(R.id.btnChoosePickup)
        val fabMapLayer = findViewById<FloatingActionButton>(R.id.fabMapLayer)

        val openSearchIntent = {
            val intent = Intent(this, SearchDropoffActivity::class.java)
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
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 16f))

        try {
            mMap.isMyLocationEnabled = true
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))
                    mMap.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("Your Real-Time Live Location")
                            .snippet("GPS Verified")
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // Add Active TODA Driver Markers
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
