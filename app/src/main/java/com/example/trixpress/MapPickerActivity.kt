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

    private var step = 1 // 1 = Pick-up, 2 = Drop-off
    private var pickupLatLng: LatLng? = null
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

        fabMapType.setOnClickListener {
            currentMapTypeIndex = (currentMapTypeIndex + 1) % mapTypes.size
            mMap.mapType = mapTypes[currentMapTypeIndex]
            Toast.makeText(this, "Map View: ${mapTypeNames[currentMapTypeIndex]}", Toast.LENGTH_SHORT).show()
        }

        btnConfirm.setOnClickListener {
            val centerLatLng = mMap.cameraPosition.target

            lifecycleScope.launch {
                val resolvedName = getLocationNameFromLatLng(centerLatLng)
                if (step == 1) {
                    pickupLatLng = centerLatLng
                    pickupLocationName = resolvedName
                    pickupMarker?.remove()
                    pickupMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(centerLatLng)
                            .title("📍 Pick-up: $pickupLocationName")
                            .snippet("Confirmed Origin")
                    )
                    pickupMarker?.showInfoWindow()

                    step = 2
                    tvInstruction.text = "Step 2 of 2: Pin your DROP-OFF location"
                    tvSummary.text = "Pick-up Set: $pickupLocationName\nNow position pin at your Destination."
                    btnConfirm.text = "Confirm Drop-off & Review Fare"
                    layoutServiceAndMode.visibility = View.VISIBLE
                    Toast.makeText(this@MapPickerActivity, "Pick-up set: $pickupLocationName", Toast.LENGTH_SHORT).show()
                } else {
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

                    val origin = pickupLatLng!!
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

                    // Return back to SearchDropoffActivity with exact locations and calculated fare
                    val intent = Intent(this@MapPickerActivity, SearchDropoffActivity::class.java).apply {
                        putExtra("PICKUP_LOCATION", pickupLocationName)
                        putExtra("DROPOFF_LOCATION", dropoffLocationName)
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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16f))

        lifecycleScope.launch {
            val initialName = getLocationNameFromLatLng(defaultLocation)
            pickupMarker = mMap.addMarker(
                MarkerOptions()
                    .position(defaultLocation)
                    .title("📍 Pick-up: $initialName")
                    .snippet("Move map or tap anywhere to set location")
            )
            pickupMarker?.showInfoWindow()
        }

        // Enable live GPS user location tracking on map
        try {
            mMap.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))
                    pickupLatLng = userLatLng
                    lifecycleScope.launch {
                        pickupLocationName = getLocationNameFromLatLng(userLatLng)
                        pickupMarker?.position = userLatLng
                        pickupMarker?.title = "📍 Pick-up (Your Real Location): $pickupLocationName"
                        pickupMarker?.showInfoWindow()
                        findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Your Real Location: $pickupLocationName"
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        mMap.setOnMapClickListener { latLng ->
            lifecycleScope.launch {
                val locName = getLocationNameFromLatLng(latLng)
                if (step == 1) {
                    pickupMarker?.remove()
                    pickupLatLng = latLng
                    pickupLocationName = locName
                    pickupMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("📍 Pick-up: $locName")
                            .snippet("Tap Confirm to set this location")
                    )
                    pickupMarker?.showInfoWindow()
                    findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Pick-up Location: $locName"
                } else {
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
        }

        mMap.setOnCameraIdleListener {
            val centerLatLng = mMap.cameraPosition.target
            lifecycleScope.launch {
                val centerName = getLocationNameFromLatLng(centerLatLng)
                if (step == 1) {
                    pickupLocationName = centerName
                    pickupMarker?.position = centerLatLng
                    pickupMarker?.title = "📍 Pick-up: $centerName"
                    findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Pick-up Location: $centerName"
                } else {
                    dropoffLocationName = centerName
                    dropoffMarker?.position = centerLatLng
                    dropoffMarker?.title = "🏁 Drop-off: $centerName"
                    findViewById<TextView>(R.id.tvSelectedLocationSummary).text = "Drop-off Destination: $centerName"
                }
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
                val addressLine = address.getAddressLine(0)
                if (!addressLine.isNullOrBlank()) {
                    return@withContext addressLine
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext "Nasugbu Location (${String.format(Locale.getDefault(), "%.4f", latLng.latitude)}, ${String.format(Locale.getDefault(), "%.4f", latLng.longitude)})"
    }
}
