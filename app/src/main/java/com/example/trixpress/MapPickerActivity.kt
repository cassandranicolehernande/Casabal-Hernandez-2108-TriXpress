package com.example.trixpress

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val defaultLocation = GeoPoint(14.0728, 120.6325) // BatStateU ARASOF Campus, Nasugbu

    private var step = 1 // 1 = Pick-up, 2 = Drop-off
    private var pickupGeoPoint: GeoPoint? = null
    private var dropoffGeoPoint: GeoPoint? = null

    private var pickupLocationName: String = "BatStateU ARASOF Campus, Nasugbu"
    private var dropoffLocationName: String = "Nasugbu Public Market, Town Proper"

    private var pickupMarker: Marker? = null
    private var dropoffMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_map_picker)

        mapView = findViewById(R.id.pickerMap)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(defaultLocation)

        val tvInstruction = findViewById<TextView>(R.id.tvMapInstruction)
        val tvSummary = findViewById<TextView>(R.id.tvSelectedLocationSummary)
        val btnConfirm = findViewById<Button>(R.id.btnMapConfirm)
        val fabMapType = findViewById<FloatingActionButton>(R.id.fabMapType)
        val layoutServiceAndMode = findViewById<LinearLayout>(R.id.layoutServiceAndMode)

        val rgService = findViewById<RadioGroup>(R.id.rgMapServiceType)
        val rgMode = findViewById<RadioGroup>(R.id.rgMapRideMode)

        var isTopo = false
        fabMapType.setOnClickListener {
            isTopo = !isTopo
            mapView.setTileSource(if (isTopo) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK)
            mapView.invalidate()
            Toast.makeText(this, if (isTopo) "Map View: Topographic" else "Map View: Standard Mapnik", Toast.LENGTH_SHORT).show()
        }

        pickupMarker = Marker(mapView).apply {
            position = defaultLocation
            title = "📍 Pick-up Location"
        }
        mapView.overlays.add(pickupMarker)
        mapView.invalidate()

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userPoint = GeoPoint(location.latitude, location.longitude)
                    mapView.controller.animateTo(userPoint)
                    mapView.controller.setZoom(17.0)
                    pickupGeoPoint = userPoint
                    pickupMarker?.position = userPoint
                    lifecycleScope.launch {
                        pickupLocationName = getAddressNameFromGeoPoint(userPoint)
                        pickupMarker?.title = "📍 Pick-up: $pickupLocationName"
                        tvSummary.text = "Your Real Location: $pickupLocationName"
                        mapView.invalidate()
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        btnConfirm.setOnClickListener {
            val centerPoint = GeoPoint(mapView.mapCenter.latitude, mapView.mapCenter.longitude)

            lifecycleScope.launch {
                val resolvedName = getAddressNameFromGeoPoint(centerPoint)
                if (step == 1) {
                    pickupGeoPoint = centerPoint
                    pickupLocationName = resolvedName
                    pickupMarker?.position = centerPoint
                    pickupMarker?.title = "📍 Pick-up: $pickupLocationName"

                    step = 2
                    tvInstruction.text = "Step 2 of 2: Pin your DROP-OFF location"
                    tvSummary.text = "Pick-up Set: $pickupLocationName\nNow position pin at your Destination."
                    btnConfirm.text = "Confirm Drop-off & Review Fare"
                    layoutServiceAndMode.visibility = View.VISIBLE
                    Toast.makeText(this@MapPickerActivity, "Pick-up set: $pickupLocationName", Toast.LENGTH_SHORT).show()
                } else {
                    dropoffGeoPoint = centerPoint
                    dropoffLocationName = resolvedName
                    dropoffMarker?.let { mapView.overlays.remove(it) }
                    dropoffMarker = Marker(mapView).apply {
                        position = centerPoint
                        title = "🏁 Drop-off: $dropoffLocationName"
                    }
                    mapView.overlays.add(dropoffMarker)
                    mapView.invalidate()

                    val origin = pickupGeoPoint ?: defaultLocation
                    val destination = dropoffGeoPoint ?: defaultLocation

                    val results = FloatArray(1)
                    Location.distanceBetween(
                        origin.latitude, origin.longitude,
                        destination.latitude, destination.longitude,
                        results
                    )
                    val distanceKm = results[0] / 1000f
                    val calculatedFare = 40.0 + (distanceKm * 15.0)
                    val formattedFare = "₱" + String.format(Locale.getDefault(), "%.2f", calculatedFare)

                    val serviceType = when (rgService.checkedRadioButtonId) {
                        R.id.rbMapDelivery -> "Express Parcel Delivery"
                        R.id.rbMapPakyaw -> "Pakyaw Charter"
                        else -> "Standard Tricycle Ride"
                    }

                    val mode = if (rgMode.checkedRadioButtonId == R.id.rbMapKasya) "Kasya kami riyan!" else "Pasabay lang po!"

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

    private suspend fun getAddressNameFromGeoPoint(geoPoint: GeoPoint): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(this@MapPickerActivity, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val line = address.getAddressLine(0)
                if (!line.isNullOrBlank()) return@withContext line
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext "Nasugbu Location (${String.format(Locale.getDefault(), "%.4f", geoPoint.latitude)}, ${String.format(Locale.getDefault(), "%.4f", geoPoint.longitude)})"
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
