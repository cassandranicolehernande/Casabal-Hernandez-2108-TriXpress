package com.example.trixpress

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.LocationServices
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PassengerDashboardActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var drawerLayout: DrawerLayout
    private val passengerLocation = GeoPoint(14.0728, 120.6325) // BatStateU ARASOF Campus, Nasugbu

    private val nearbyDrivers = listOf(
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", GeoPoint(14.0730, 120.6328), "₱40.00"),
        TricycleDriver("Kuya Lito", "ARASOF Campus TODA", GeoPoint(14.0735, 120.6332), "₱40.00"),
        TricycleDriver("Kuya Cardo", "ARASOF Campus TODA", GeoPoint(14.0722, 120.6318), "₱40.00"),
        TricycleDriver("Kuya Juaning", "Town Proper TODA", GeoPoint(14.0740, 120.6345), "₱45.00"),
        TricycleDriver("Kuya Dodong", "Town Proper TODA", GeoPoint(14.0745, 120.6350), "₱45.00"),
        TricycleDriver("Kuya Mario", "Bunducan TODA", GeoPoint(14.0755, 120.6360), "₱50.00"),
        TricycleDriver("Kuya Ben", "Bunducan TODA", GeoPoint(14.0760, 120.6365), "₱50.00"),
        TricycleDriver("Kuya Nilo", "Wawa Port TODA", GeoPoint(14.0780, 120.6380), "₱55.00"),
        TricycleDriver("Kuya Mark", "Natipuan TODA", GeoPoint(14.0820, 120.6400), "₱60.00"),
        TricycleDriver("Kuya Jun", "Calayo Beach TODA", GeoPoint(14.0880, 120.6420), "₱65.00")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_passenger_dashboard)

        mapView = findViewById(R.id.map)
        MapTileHelper.applyDefaultStreetTiles(mapView)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(passengerLocation)

        val userMarker = Marker(mapView).apply {
            position = passengerLocation
            title = "Your Location"
            snippet = "BatStateU ARASOF Campus"
        }
        mapView.overlays.add(userMarker)

        for (driver in nearbyDrivers) {
            val dMarker = Marker(mapView).apply {
                position = driver.location
                title = "${driver.name} (${driver.toda})"
                snippet = "Fare: ${driver.fare} • Rating: 4.9★"
            }
            mapView.overlays.add(dMarker)
        }
        mapView.invalidate()

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userPoint = GeoPoint(location.latitude, location.longitude)
                    mapView.controller.animateTo(userPoint)
                    mapView.controller.setZoom(17.0)
                    userMarker.position = userPoint
                    mapView.invalidate()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnOpenDrawer = findViewById<ImageView>(R.id.btnOpenDrawer)

        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).apply { putExtra("IS_DRIVER", false) })
                }
                R.id.nav_zones -> {
                    startActivity(Intent(this, ServiceZoneActivity::class.java))
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
            startActivity(Intent(this, SearchDropoffActivity::class.java))
        }

        cardTopSearch.setOnClickListener { openSearchIntent() }
        btnChoosePickup.setOnClickListener { openSearchIntent() }

        var isTopo = false
        fabMapLayer.setOnClickListener {
            isTopo = !isTopo
            MapTileHelper.toggleStreetAndTopo(mapView, isTopo)
            Toast.makeText(
                this,
                if (isTopo) "Map View: Topographic" else "Map View: Standard Street",
                Toast.LENGTH_SHORT
            ).show()
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