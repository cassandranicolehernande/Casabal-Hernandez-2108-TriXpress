package com.example.trixpress

import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Locale

class SearchDropoffActivity : AppCompatActivity() {

    private val nasugbuLandmarks = arrayOf(
        "BatStateU ARASOF Campus, Nasugbu",
        "Nasugbu Public Market, Town Proper",
        "Nasugbu Municipal Hall",
        "Jollibee Nasugbu Crossing",
        "St. Francis of Assisi Parish Church",
        "Barangay 1, Town Proper, Nasugbu",
        "Barangay 2, Wawa, Nasugbu",
        "Barangay 3, Bucana, Nasugbu",
        "Barangay 4, Talangan, Nasugbu",
        "Barangay 5, Putat, Nasugbu",
        "Barangay 6, Kaylaway, Nasugbu",
        "Barangay 7, Pantalan, Nasugbu",
        "Barangay 8, Natipuan, Nasugbu",
        "Barangay 9, Looc, Nasugbu",
        "Barangay 10, Bulihan, Nasugbu",
        "Barangay 11, Malapad na Bato, Nasugbu",
        "Calayo Beach Resort, Nasugbu",
        "Natipuan Beach, Nasugbu",
        "Wawa Port, Nasugbu",
        "Bunducan Elementary School",
        "Cala de Mar / Peninsula, Nasugbu",
        "Hamilo Coast / Pico de Loro",
        "Ternate-Nasugbu Highway Junction",
        "Magallanes - Nasugbu Road",
        "East-West Road View Deck, Nasugbu"
    )

    private lateinit var etPickup: AutoCompleteTextView
    private lateinit var etDropoff: AutoCompleteTextView
    private lateinit var tvEstimatedFare: TextView
    private lateinit var cbRain: CheckBox
    private lateinit var cbRaincoat: CheckBox
    private lateinit var cbUmbrella: CheckBox
    private lateinit var cbInsurance: CheckBox
    private lateinit var etDriverNotes: EditText
    private lateinit var layoutPassengerCount: View
    private lateinit var layoutParcelDetails: View
    private lateinit var etParcelDescription: EditText
    private lateinit var btnAttachParcelPhoto: Button
    private lateinit var tvPhotoStatus: TextView
    private lateinit var rgServiceType: RadioGroup

    private var baseFare = 50.0
    private var selectedTip = 0.0
    private var isPhotoAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_dropoff)

        etPickup = findViewById(R.id.etSearchPickup)
        etDropoff = findViewById(R.id.etSearchDropoff)
        tvEstimatedFare = findViewById(R.id.tvEstimatedFare)
        cbRain = findViewById(R.id.cbRain)
        cbRaincoat = findViewById(R.id.cbRaincoat)
        cbUmbrella = findViewById(R.id.cbUmbrella)
        cbInsurance = findViewById(R.id.cbInsurance)
        etDriverNotes = findViewById(R.id.etDriverNotes)
        layoutPassengerCount = findViewById(R.id.layoutPassengerCount)
        layoutParcelDetails = findViewById(R.id.layoutParcelDetails)
        etParcelDescription = findViewById(R.id.etParcelDescription)
        btnAttachParcelPhoto = findViewById(R.id.btnAttachParcelPhoto)
        tvPhotoStatus = findViewById(R.id.tvPhotoStatus)
        rgServiceType = findViewById(R.id.rgServiceType)

        // Frequent Places Chips
        findViewById<Button>(R.id.chipHome).setOnClickListener { etDropoff.setText("Barangay 1, Town Proper, Nasugbu", false) }
        findViewById<Button>(R.id.chipCampus).setOnClickListener { etDropoff.setText("BatStateU ARASOF Campus, Nasugbu", false) }
        findViewById<Button>(R.id.chipMarket).setOnClickListener { etDropoff.setText("Nasugbu Public Market, Town Proper", false) }
        findViewById<Button>(R.id.chipWawa).setOnClickListener { etDropoff.setText("Wawa Port, Nasugbu", false) }
        findViewById<Button>(R.id.chipCalayo).setOnClickListener { etDropoff.setText("Calayo Beach Resort, Nasugbu", false) }

        // Quick Message Chips for Driver ("Pakibilis po!", etc.)
        findViewById<Button>(R.id.chipFast).setOnClickListener { etDriverNotes.setText("Pakibilis po, medyo nagmamadali ako!") }
        findViewById<Button>(R.id.chipGate).setOnClickListener { etDriverNotes.setText("Nasa gate na po ako, paki-sundo po.") }
        findViewById<Button>(R.id.chipMarketBag).setOnClickListener { etDriverNotes.setText("May dala pong market sacks/bags, paki-tulungan po.") }
        findViewById<Button>(R.id.chipCorner).setOnClickListener { etDriverNotes.setText("Malapit na po ako sa kanto.") }

        btnAttachParcelPhoto.setOnClickListener {
            isPhotoAttached = true
            tvPhotoStatus.text = "📷 Parcel Photo Attached (Ready for Driver Sizing)"
            Toast.makeText(this, "Parcel photo attached successfully!", Toast.LENGTH_SHORT).show()
        }

        val btnTip10 = findViewById<Button>(R.id.btnTip10)
        val btnTip20 = findViewById<Button>(R.id.btnTip20)
        val btnTip50 = findViewById<Button>(R.id.btnTip50)
        val btnTip100 = findViewById<Button>(R.id.btnTip100)

        val tipButtons = listOf(btnTip10, btnTip20, btnTip50, btnTip100)
        val tipValues = listOf(10.0, 20.0, 50.0, 100.0)

        for (i in tipButtons.indices) {
            tipButtons[i].setOnClickListener {
                selectedTip = tipValues[i]
                for (b in tipButtons) b.setBackgroundColor(android.graphics.Color.WHITE)
                tipButtons[i].setBackgroundColor(0xFFFFC107.toInt())
                recalculateTotalFare()
                Toast.makeText(this, "Added ₱${tipValues[i].toInt()} tip for Kuya Driver!", Toast.LENGTH_SHORT).show()
            }
        }

        rgServiceType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbServicePakyaw -> {
                    layoutPassengerCount.visibility = View.VISIBLE
                    layoutParcelDetails.visibility = View.GONE
                    baseFare = 120.0
                    Toast.makeText(this, "Pakyaw Charter Selected: Choose passenger count", Toast.LENGTH_SHORT).show()
                }
                R.id.rbServiceDelivery -> {
                    layoutPassengerCount.visibility = View.GONE
                    layoutParcelDetails.visibility = View.VISIBLE
                    baseFare = 65.0
                    Toast.makeText(this, "Express Parcel Delivery Selected: Type details & attach photo", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    layoutPassengerCount.visibility = View.GONE
                    layoutParcelDetails.visibility = View.GONE
                    baseFare = 50.0
                }
            }
            recalculateTotalFare()
        }

        findViewById<RadioGroup>(R.id.rgPassengerCount)?.setOnCheckedChangeListener { _, _ ->
            recalculateTotalFare()
        }

        cbRain.setOnCheckedChangeListener { _, _ -> recalculateTotalFare() }
        cbRaincoat.setOnCheckedChangeListener { _, _ -> recalculateTotalFare() }
        cbUmbrella.setOnCheckedChangeListener { _, _ -> recalculateTotalFare() }
        cbInsurance.setOnCheckedChangeListener { _, _ -> recalculateTotalFare() }

        val adapter = ArrayAdapter(this, R.layout.item_dropdown_suggestion, nasugbuLandmarks)
        etPickup.setAdapter(adapter)
        etDropoff.setAdapter(adapter)

        etPickup.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)
        etDropoff.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)

        // Fetch real-time device GPS location for pickup automatically
        val defaultPickup = intent.getStringExtra("PICKUP_LOCATION")
        if (defaultPickup.isNullOrBlank()) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(this)
                fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        lifecycleScope.launch {
                            val addressName = getAddressNameFromGeoPoint(GeoPoint(location.latitude, location.longitude))
                            etPickup.setText(addressName, false)
                        }
                    } else {
                        etPickup.setText("Current Live GPS Location", false)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                etPickup.setText("Current Live GPS Location", false)
            }
        } else {
            etPickup.setText(defaultPickup, false)
        }

        val launchMapPicker = {
            startActivity(Intent(this, MapPickerActivity::class.java))
        }

        etPickup.setOnClickListener { launchMapPicker() }
        etPickup.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) launchMapPicker() }

        etDropoff.setOnClickListener { launchMapPicker() }
        etDropoff.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) launchMapPicker() }

        val rgRideMode = findViewById<RadioGroup>(R.id.rgRideMode)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)
        val btnSearchInGoogleMaps = findViewById<Button>(R.id.btnSearchInGoogleMaps)

        btnSearchInGoogleMaps?.setOnClickListener {
            val pickup = etPickup.text.toString().trim().ifEmpty { "Current Live GPS Location" }
            val dropoff = etDropoff.text.toString().trim().ifEmpty { "Nasugbu Public Market" }
            val mapsUri = android.net.Uri.parse(
                "https://www.google.com/maps/dir/?api=1&origin=${android.net.Uri.encode(pickup)}&destination=${android.net.Uri.encode(dropoff)}&travelmode=driving"
            )
            try {
                startActivity(Intent(Intent.ACTION_VIEW, mapsUri).setPackage("com.google.android.apps.maps"))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
            }
        }

        btnConfirmBooking.setOnClickListener {
            val pickup = etPickup.text.toString().trim()
            val dropoff = etDropoff.text.toString().trim()
            val notes = etDriverNotes.text.toString().trim()
            val parcelDesc = etParcelDescription.text.toString().trim()

            if (pickup.isEmpty()) {
                Toast.makeText(this, "Please enter or pick your pick-up address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dropoff.isEmpty()) {
                Toast.makeText(this, "Please enter or pick your drop-off address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviceType = when (rgServiceType.checkedRadioButtonId) {
                R.id.rbServiceDelivery -> {
                    if (parcelDesc.isEmpty()) {
                        Toast.makeText(this, "Please describe the parcel to be delivered", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    "Express Parcel Delivery ($parcelDesc)"
                }
                R.id.rbServicePakyaw -> {
                    val paxCount = when (findViewById<RadioGroup>(R.id.rgPassengerCount)?.checkedRadioButtonId) {
                        R.id.rbPax1 -> "1 Passenger"
                        R.id.rbPax2 -> "2 Passengers"
                        R.id.rbPax3 -> "3 Passengers"
                        else -> "4 Passengers"
                    }
                    "Group Pakyaw Charter ($paxCount)"
                }
                else -> "Individual Ride"
            }

            val finalNotes = if (isPhotoAttached) {
                "$notes [📷 Parcel Photo Attached for Driver Sizing]"
            } else {
                notes
            }

            val mode = if (rgRideMode.checkedRadioButtonId == R.id.rbGroup) "Group" else "Individual Passenger"
            val totalFare = tvEstimatedFare.text.toString()

            val pickupGeoPoint = getGeoPointForLandmark(pickup)
            val dropoffGeoPoint = getGeoPointForLandmark(dropoff)

            startLookingForDriverSequence(pickup, dropoff, serviceType, mode, totalFare, finalNotes, pickupGeoPoint, dropoffGeoPoint)
        }
    }

    private suspend fun getAddressNameFromGeoPoint(geoPoint: GeoPoint): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(this@SearchDropoffActivity, Locale.getDefault())
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
        return@withContext "Current Live GPS Location (${String.format(Locale.getDefault(), "%.4f", geoPoint.latitude)}, ${String.format(Locale.getDefault(), "%.4f", geoPoint.longitude)})"
    }

    private fun getGeoPointForLandmark(name: String): GeoPoint {
        val lower = name.lowercase(Locale.getDefault())
        return when {
            lower.contains("arasof") || lower.contains("campus") -> GeoPoint(14.0728, 120.6325)
            lower.contains("market") || lower.contains("town proper") -> GeoPoint(14.0745, 120.6360)
            lower.contains("municipal") -> GeoPoint(14.0730, 120.6340)
            lower.contains("jollibee") -> GeoPoint(14.0715, 120.6330)
            lower.contains("church") -> GeoPoint(14.0735, 120.6350)
            lower.contains("wawa") -> GeoPoint(14.0780, 120.6380)
            lower.contains("calayo") -> GeoPoint(14.0880, 120.6420)
            lower.contains("natipuan") -> GeoPoint(14.0820, 120.6400)
            lower.contains("bunducan") -> GeoPoint(14.0755, 120.6360)
            lower.contains("looc") -> GeoPoint(14.1200, 120.6400)
            lower.contains("bulihan") -> GeoPoint(14.0850, 120.6420)
            lower.contains("barangay 1") -> GeoPoint(14.0732, 120.6345)
            lower.contains("barangay 2") -> GeoPoint(14.0780, 120.6380)
            lower.contains("barangay 3") -> GeoPoint(14.0689, 120.6295)
            lower.contains("barangay 4") -> GeoPoint(14.0728, 120.6325)
            lower.contains("barangay 5") -> GeoPoint(14.0752, 120.6358)
            else -> GeoPoint(14.0745, 120.6360)
        }
    }

    private fun recalculateTotalFare() {
        var total = baseFare
        if (rgServiceType.checkedRadioButtonId == R.id.rbServicePakyaw) {
            val paxExtra = when (findViewById<RadioGroup>(R.id.rgPassengerCount)?.checkedRadioButtonId) {
                R.id.rbPax1 -> 0.0
                R.id.rbPax2 -> 20.0
                R.id.rbPax3 -> 40.0
                else -> 60.0
            }
            total += paxExtra
        }
        if (cbRain.isChecked) total += 20.0
        if (cbRaincoat.isChecked) total += 19.0
        if (cbUmbrella.isChecked) total += 15.0
        if (cbInsurance.isChecked) total += 10.0
        total += selectedTip
        tvEstimatedFare.text = "₱" + String.format(Locale.getDefault(), "%.2f", total)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val pickup = intent.getStringExtra("PICKUP_LOCATION")
        val dropoff = intent.getStringExtra("DROPOFF_LOCATION")
        val fareStr = intent.getStringExtra("EST_FARE")
        if (!pickup.isNullOrBlank()) etPickup.setText(pickup, false)
        if (!dropoff.isNullOrBlank()) etDropoff.setText(dropoff, false)
        if (!fareStr.isNullOrBlank()) {
            val clean = fareStr.replace("₱", "").trim().toDoubleOrNull()
            if (clean != null) {
                baseFare = clean
                recalculateTotalFare()
            }
        }
    }

    private fun startLookingForDriverSequence(
        pickup: String,
        dropoff: String,
        serviceType: String,
        mode: String,
        fare: String,
        notes: String,
        pickupGeoPoint: GeoPoint,
        dropoffGeoPoint: GeoPoint
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_searching_driver, null)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvSearchStatus)
        val tvSub = dialogView.findViewById<TextView>(R.id.tvSearchSub)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.show()

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            tvStatus.text = "Checking TODA Service Zones..."
            tvSub.text = if (notes.isNotEmpty()) "Note for Driver: \"$notes\"" else "Sending booking request..."
        }, 2000)

        handler.postDelayed({
            tvStatus.text = "Calculating optimal route..."
            tvSub.text = "Estimated Fare: $fare ($mode)"
        }, 4500)

        handler.postDelayed({
            tvStatus.text = "Driver Found! Assigning Kuya Roberto"
            tvSub.text = "Kuya Roberto (ARASOF TODA) accepted your trip."
            handler.postDelayed({
                dialog.dismiss()
                val intent = Intent(this, TrackingActivity::class.java).apply {
                    putExtra("DRIVER_NAME", "Kuya Roberto (ARASOF TODA)")
                    putExtra("PICKUP_LOC", pickup)
                    putExtra("DEST_LOC", dropoff)
                    putExtra("PICKUP_LAT", pickupGeoPoint.latitude)
                    putExtra("PICKUP_LNG", pickupGeoPoint.longitude)
                    putExtra("DEST_LAT", dropoffGeoPoint.latitude)
                    putExtra("DEST_LNG", dropoffGeoPoint.longitude)
                    putExtra("SERVICE_TYPE", serviceType)
                    putExtra("BOOKING_MODE", mode)
                    putExtra("EST_FARE", fare)
                    putExtra("DRIVER_NOTES", notes)
                }
                startActivity(intent)
                finish()
            }, 1200)
        }, 5000)
    }
}
