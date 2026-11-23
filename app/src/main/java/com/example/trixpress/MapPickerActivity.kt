package com.example.trixpress

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val defaultLocation = LatLng(14.0728, 120.6325) // BatStateU ARASOF Campus, Nasugbu

    private var step = 2 // Start directly in Drop-Off selection mode
    private var pickupLatLng: LatLng = defaultLocation
    private var dropoffLatLng: LatLng? = null

    private var pickupLocationName: String = "BatStateU ARASOF Campus, Nasugbu"
    private var dropoffLocationName: String = "Nasugbu Public Market, Town Proper"

    private var pickupMarker: Marker? = null
    private var dropoffMarker: Marker? = null

    private var currentMapTypeIndex = 0
    private val mapTypes = intArrayOf(
        GoogleMap.MAP_TYPE_NORMAL,
        GoogleMap.MAP_TYPE_SATELLITE,
        GoogleMap.MAP_TYPE_TERRAIN,
        GoogleMap.MAP_TYPE_HYBRID
    )
    private val mapTypeNames = arrayOf("Normal", "Satellite", "Terrain", "Hybrid")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.pickerMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val tvInstruction = findViewById<TextView>(R.id.tvMapInstruction)
        val tvSummary = findViewById<TextView>(R.id.tvSelectedLocationSummary)
        val btnConfirm = findViewById<Button>(R.id.btnMapConfirm)
        val fabMapType = findViewById<FloatingActionButton>(R.id.fabMapType)
        val layoutServiceAndMode = findViewById<LinearLayout>(R.id.layoutServiceAndMode)

        val rgService = findViewById<RadioGroup>(R.id.rgMapServiceType)
        val rgMode = findViewById<RadioGroup>(R.id.rgMapRideMode)

        // Streamlined Drop-Off Selection Mode
        tvInstruction.text = "Pin your DROP-OFF location on the map"
        btnConfirm.text = "Confirm Drop-off & Review Fare"
        layoutServiceAndMode.visibility = View.VISIBLE

        fabMapType.setOnClickListener {
            currentMapTypeIndex = (currentMapTypeIndex + 1) % mapTypes.size
            mMap.mapType = mapTypes[currentMapTypeIndex]
            Toast.makeText(this, "Map View: ${mapTypeNames[currentMapTypeIndex]}", Toast.LENGTH_SHORT).show()
        }

        btnConfirm.setOnClickListener {
            val centerLatLng = mMap.cameraPosition.target

            lifecycleScope.launch {
                val resolvedName = getLocationNameFromLatLng(centerLatLng)
                dropoffLatLng = centerLatLng
                dropoffLocationName = resolvedName
                dropoffMarker?.remove()
                dropoffMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(centerLatLng)
                        .title("🏁 Drop-off: $dropoffLocationName")
                        .snippet("Confirmed Destination")
                )
                dropoffMarker?.showInfoWindow()

                val origin = pickupLatLng
                val destination = dropoffLatLng!!

                val results = FloatArray(1)
                Location.distanceBetween(
                    origin.latitude, origin.longitude,
                    destination.latitude, destination.longitude,
                    results
                )
                val distanceKm = results[0] / 1000f
                val baseFare = 40.0
                val calculatedFare = baseFare + (distanceKm * 15.0)
                val formattedFare = "₱" + String.format(Locale.getDefault(), "%.2f", calculatedFare)

                val serviceType = when (rgService.checkedRadioButtonId) {
                    R.id.rbMapDelivery -> "Express Parcel Delivery"
                    R.id.rbMapPakyaw -> "Pakyaw Charter"
                    else -> "Standard Tricycle Ride"
                }

                val mode = if (rgMode.checkedRadioButtonId == R.id.rbMapKasya) {
                    "Kasya kami riyan!"
                } else {
                    "Pasabay lang po!"
                }

                val intent = Intent(this@MapPickerActivity, SearchDropoffActivity::class.java).apply {
                    putExtra("PICKUP_LOCATION", pickupLocationName)
                    putExtra("DROPOFF_LOCATION", dropoffLocationName)
                    putExtra("PICKUP_LAT", origin.latitude)
                    putExtra("PICKUP_LNG", origin.longitude)
                    putExtra("DEST_LAT", destination.latitude)
                    putExtra("DEST_LNG", destination.longitude)
                    putExtra("EST_FARE", formattedFare)
                    putExtra("SERVICE_TYPE", serviceType)
                    putExtra("BOOKING_MODE", mode)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16f))

        try {
            mMap.isMyLocationEnabled = true
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            val token = CancellationTokenSource().token
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token)
                .addOnSuccessListener { location: Location? ->
                    val targetLoc = location ?: Location("").apply {
                        latitude = defaultLocation.latitude
                        longitude = defaultLocation.longitude
                    }
                    val userLatLng = LatLng(targetLoc.latitude, targetLoc.longitude)
                    pickupLatLng = userLatLng
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))

                    lifecycleScope.launch {
                        pickupLocationName = getLocationNameFromLatLng(userLatLng)
                        pickupMarker?.remove()
                        pickupMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("📍 Pick-up (Your Real Location): $pickupLocationName")
                                .snippet("Live GPS Position")
                        )
                        pickupMarker?.showInfoWindow()
                        findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Pick-up: $pickupLocationName\nPin your Drop-off location."
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        mMap.setOnMapClickListener { latLng ->
            lifecycleScope.launch {
                val locName = getLocationNameFromLatLng(latLng)
                dropoffMarker?.remove()
                dropoffLatLng = latLng
                dropoffLocationName = locName
                dropoffMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("🏁 Drop-off: $locName")
                        .snippet("Tap Confirm to complete booking")
                )
                dropoffMarker?.showInfoWindow()
                findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Drop-off Destination: $locName"
            }
        }

        mMap.setOnCameraIdleListener {
            val centerLatLng = mMap.cameraPosition.target
            lifecycleScope.launch {
                val centerName = getLocationNameFromLatLng(centerLatLng)
                dropoffLocationName = centerName
                dropoffMarker?.position = centerLatLng
                dropoffMarker?.title = "🏁 Drop-off: $centerName"
                findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Drop-off Destination: $centerName"
            }
        }
    }

    private suspend fun getLocationNameFromLatLng(latLng: LatLng): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(this@MapPickerActivity, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val line = address.getAddressLine(0)
                if (!line.isNullOrBlank()) return@withContext line
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext NasugbuLocations.findNearestPlaceName(latLng.latitude, latLng.longitude)
    }
}
