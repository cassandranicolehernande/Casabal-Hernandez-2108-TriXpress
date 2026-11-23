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
    private val passengerLocation = LatLng(14.0673, 120.6262) // BatStateU ARASOF - Nasugbu Campus

    // Active TODA Drivers positioned at exact user-provided Nasugbu landmark coordinates
    private val nearbyDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", LatLng(14.0673, 120.6262), "₱40.00"),
        TricycleDriver("Kuya Lito", "Town Proper TODA", LatLng(14.0736, 120.6322), "₱40.00"),
        TricycleDriver("Kuya Cardo", "Town Plaza TODA", LatLng(14.0731, 120.6320), "₱40.00"),
        TricycleDriver("Kuya Juaning", "Public Market TODA", LatLng(14.0754, 120.6358), "₱45.00"),
        TricycleDriver("Kuya Dodong", "Jollibee Crossing TODA", LatLng(14.0744, 120.6346), "₱45.00"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", LatLng(14.0948, 120.6480), "₱50.00"),
        TricycleDriver("Kuya Nilo", "Wawa Port TODA", LatLng(14.0845, 120.6248), "₱55.00"),
        TricycleDriver("Kuya Mark", "Natipuan TODA", LatLng(14.1283, 120.6019), "₱60.00"),
        TricycleDriver("Kuya Jun", "Calayo Beach TODA", LatLng(14.1485, 120.6115), "₱65.00")
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
                    Toast.makeText(this, "Map View: Standard Street", Toast.LENGTH_SHORT).show()
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
        mMap.isTrafficEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 16f))

        try {
            mMap.isMyLocationEnabled = true
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

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
