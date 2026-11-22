package com.example.trixpress

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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

            if (step == 1) {
                pickupLatLng = centerLatLng
                pickupLocationName = getLocationNameFromLatLng(centerLatLng)
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
                btnConfirm.text = "Confirm Drop-off & Book"
                layoutServiceAndMode.visibility = View.VISIBLE
                Toast.makeText(this, "Pick-up location set: $pickupLocationName", Toast.LENGTH_SHORT).show()
            } else {
                dropoffLatLng = centerLatLng
                dropoffLocationName = getLocationNameFromLatLng(centerLatLng)
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

                startProfessionalDriverMatchingSequence(serviceType, mode, formattedFare, pickupLocationName, dropoffLocationName)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16f))

        val initialName = getLocationNameFromLatLng(defaultLocation)
        pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(defaultLocation)
                .title("📍 Pick-up: $initialName")
                .snippet("Move map or tap anywhere to set location")
        )
        pickupMarker?.showInfoWindow()

        mMap.setOnMapClickListener { latLng ->
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

        mMap.setOnCameraIdleListener {
            val centerLatLng = mMap.cameraPosition.target
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

    private fun getLocationNameFromLatLng(latLng: LatLng): String {
        return when {
            latLng.latitude in 14.0720..14.0735 && latLng.longitude in 120.6315..120.6335 -> "BatStateU ARASOF Campus, Nasugbu"
            latLng.latitude in 14.0736..14.0760 && latLng.longitude in 120.6336..120.6370 -> "Nasugbu Public Market, Town Proper"
            latLng.latitude in 14.0710..14.0722 && latLng.longitude in 120.6320..120.6345 -> "Jollibee Nasugbu Crossing"
            latLng.latitude in 14.0730..14.0742 && latLng.longitude in 120.6345..120.6360 -> "St. Francis of Assisi Parish Church"
            else -> "Nasugbu Location (${String.format(Locale.getDefault(), "%.4f", latLng.latitude)}, ${String.format(Locale.getDefault(), "%.4f", latLng.longitude)})"
        }
    }

    private fun startProfessionalDriverMatchingSequence(
        serviceType: String,
        mode: String,
        estFare: String,
        pickupName: String,
        dropoffName: String
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_searching_driver, null)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvSearchStatus)
        val tvSub = dialogView.findViewById<TextView>(R.id.tvSearchSub)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.show()

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            tvStatus.text = "Scanning TODA Service Zones..."
            tvSub.text = "Filtering active drivers near $pickupName..."
        }, 2000)

        handler.postDelayed({
            tvStatus.text = "Calculating Fare & Route..."
            tvSub.text = "Estimated Fare: $estFare ($mode)"
        }, 4500)

        handler.postDelayed({
            tvStatus.text = "Driver Matched Successfully!"
            tvSub.text = "Kuya Roberto (ARASOF TODA) accepted your request."
            handler.postDelayed({
                dialog.dismiss()
                val intent = Intent(this, TrackingActivity::class.java).apply {
                    putExtra("DRIVER_NAME", "Kuya Roberto (ARASOF TODA - Plate: AB-1234)")
                    putExtra("PICKUP_LOC", pickupName)
                    putExtra("DEST_LOC", dropoffName)
                    putExtra("SERVICE_TYPE", serviceType)
                    putExtra("BOOKING_MODE", mode)
                    putExtra("EST_FARE", estFare)
                }
                startActivity(intent)
                finish()
            }, 1200)
        }, 7000)
    }
}
