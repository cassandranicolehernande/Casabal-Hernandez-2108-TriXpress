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

    // Active TODA Drivers positioned strictly on inland streets & solid dry land roads
    private val nearbyDrivers = listOf(
        // ARASOF Campus TODA (Inland Campus Roads)
        TricycleDriver("Kuya Roberto", "ARASOF Campus TODA", LatLng(14.0673, 120.6268), "₱40.00", "Standard Sidecar", "Honda TMX 125 Alpha", 4, "412-ABC"),
        TricycleDriver("Kuya Lito", "ARASOF Campus TODA", LatLng(14.0678, 120.6275), "₱40.00", "Extended Family Sidecar", "Kawasaki Barako II", 6, "582-DEF"),
        TricycleDriver("Kuya Benj", "ARASOF Campus TODA", LatLng(14.0665, 120.6270), "₱40.00", "Center-Car / Bajaj RE", "Bajaj RE 205", 4, "913-GHI"),

        // Town Proper & Market TODA (Poblacion Streets)
        TricycleDriver("Kuya Cardo", "Town Proper TODA", LatLng(14.0736, 120.6322), "₱45.00", "Standard Sidecar", "Honda TMX Supremo 150", 4, "204-JKL"),
        TricycleDriver("Kuya Juaning", "Town Proper TODA", LatLng(14.064070, 120.633164), "₱45.00", "Extended Family Sidecar", "Kawasaki Barako II", 6, "731-MNO"),
        TricycleDriver("Kuya Nestor", "Town Proper TODA", LatLng(14.0748, 120.6352), "₱45.00", "Center-Car / TVS King", "TVS King Deluxe", 4, "319-STU"),

        // Jollibee Crossing TODA (Palico-Nasugbu Highway)
        TricycleDriver("Kuya Dodong", "Jollibee Crossing TODA", LatLng(14.0744, 120.6346), "₱45.00", "Standard Sidecar", "Yamaha YTX 125", 4, "615-PQR"),
        TricycleDriver("Kuya Isko", "Jollibee Crossing TODA", LatLng(14.0740, 120.6340), "₱45.00", "E-Trike Passenger Unit", "BEMAC E-Trike", 6, "846-UVW"),

        // Bunducan TODA (Bunducan Main Road)
        TricycleDriver("Kuya Mario", "Bunducan TODA", LatLng(14.0945, 120.6470), "₱50.00", "Extended Family Sidecar", "Kawasaki Barako II", 6, "731-XYZ"),
        TricycleDriver("Kuya Tino", "Bunducan TODA", LatLng(14.0952, 120.6485), "₱50.00", "Standard Sidecar", "Honda TMX 125 Alpha", 4, "102-ABC"),

        // Wawa Port TODA (Wawa Inland Street - Off Water)
        TricycleDriver("Kuya Nilo", "Wawa Port TODA", LatLng(14.0845, 120.6285), "₱55.00", "E-Trike Passenger Unit", "BEMAC E-Trike", 6, "846-DEF"),
        TricycleDriver("Kuya Kiko", "Wawa Port TODA", LatLng(14.0850, 120.6290), "₱55.00", "Cargo / Kolong-Kolong", "Honda TMX Cargo", 2, "405-GHI"),

        // Bucana TODA (Bucana Inland Village Street - Off Water)
        TricycleDriver("Kuya Pido", "Bucana TODA", LatLng(14.0655, 120.6270), "₱45.00", "Standard Sidecar", "Honda TMX 125 Alpha", 4, "219-JKL"),
        TricycleDriver("Kuya Tisoy", "Bucana TODA", LatLng(14.0660, 120.6275), "₱45.00", "Extended Family Sidecar", "Kawasaki Barako II", 6, "554-MNO"),

        // Natipuan TODA (Natipuan Inland Highway)
        TricycleDriver("Kuya Mark", "Natipuan TODA", LatLng(14.1283, 120.6075), "₱60.00", "Center-Car / TVS King", "TVS King Deluxe", 4, "319-PQR"),
        TricycleDriver("Kuya Rolly", "Natipuan TODA", LatLng(14.1290, 120.6080), "₱60.00", "Extended Family Sidecar", "Kawasaki Barako II", 6, "782-STU"),

        // Calayo Beach TODA (Calayo Inland Village Street)
        TricycleDriver("Kuya Jun", "Calayo Beach TODA", LatLng(14.1485, 120.6165), "₱65.00", "Extended Family Sidecar", "Kawasaki Barako II", 6, "102-VWX"),
        TricycleDriver("Kuya Dante", "Calayo Beach TODA", LatLng(14.1490, 120.6170), "₱65.00", "Standard Sidecar", "Honda TMX Supremo", 4, "901-YZA")
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15f))

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
                    .snippet("${driver.unitModel} • ${driver.tricycleType} (${driver.seatCapacity}-Seater) • Plate: ${driver.plateNumber}")
            )
        }
    }
}
