package com.example.trixpress

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SearchDropoffActivity : AppCompatActivity() {

    private val nasugbuLandmarks = arrayOf(
        "BatStateU ARASOF Campus, Nasugbu",
        "Nasugbu Public Market, Town Proper",
        "Nasugbu Municipal Hall",
        "Jollibee Nasugbu Crossing",
        "St. Francis of Assisi Parish Church",
        "Calayo Beach Resort, Nasugbu",
        "Natipuan Beach, Nasugbu",
        "Wawa Port, Nasugbu",
        "Bunducan Elementary School",
        "Cala de Mar / Peninsula, Nasugbu",
        "Hamilo Coast / Pico de Loro",
        "Ternate-Nasugbu Highway Junction"
    )

    private lateinit var etPickup: AutoCompleteTextView
    private lateinit var etDropoff: AutoCompleteTextView
    private lateinit var tvEstimatedFare: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_dropoff)

        etPickup = findViewById(R.id.etSearchPickup)
        etDropoff = findViewById(R.id.etSearchDropoff)
        tvEstimatedFare = findViewById(R.id.tvEstimatedFare)

        val btnPickPickupOnMap = findViewById<TextView>(R.id.btnPickPickupOnMap)
        val btnPickDropoffOnMap = findViewById<TextView>(R.id.btnPickDropoffOnMap)

        val adapter = ArrayAdapter(this, R.layout.item_dropdown_suggestion, nasugbuLandmarks)
        etPickup.setAdapter(adapter)
        etDropoff.setAdapter(adapter)

        etPickup.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)
        etDropoff.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)

        val defaultPickup = intent.getStringExtra("PICKUP_LOCATION") ?: "BatStateU ARASOF Campus, Nasugbu"
        etPickup.setText(defaultPickup, false)

        etPickup.setOnClickListener { etPickup.showDropDown() }
        etDropoff.setOnClickListener { etDropoff.showDropDown() }

        // Tapping "Pick on Map" launches MapPickerActivity directly
        btnPickPickupOnMap.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            startActivity(intent)
        }

        btnPickDropoffOnMap.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            startActivity(intent)
        }

        val rgRideMode = findViewById<RadioGroup>(R.id.rgRideMode)
        val rgServiceType = findViewById<RadioGroup>(R.id.rgServiceType)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)
        val btnSearchInGoogleMaps = findViewById<Button>(R.id.btnSearchInGoogleMaps)

        btnSearchInGoogleMaps.setOnClickListener {
            val pickup = etPickup.text.toString().trim().ifEmpty { "BatStateU ARASOF Campus, Nasugbu" }
            val dropoff = etDropoff.text.toString().trim().ifEmpty { "Nasugbu Public Market" }
            val mapsUri = android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${android.net.Uri.encode(pickup)}&destination=${android.net.Uri.encode(dropoff)}&travelmode=driving")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, mapsUri)
                startActivity(browserIntent)
            }
        }

        btnConfirmBooking.setOnClickListener {
            val pickup = etPickup.text.toString().trim()
            val dropoff = etDropoff.text.toString().trim()

            if (pickup.isEmpty()) {
                Toast.makeText(this, "Please enter or pick your pick-up address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dropoff.isEmpty()) {
                Toast.makeText(this, "Please enter or pick your drop-off address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviceType = when (rgServiceType.checkedRadioButtonId) {
                R.id.rbServiceDelivery -> "Express Parcel Delivery"
                R.id.rbServicePakyaw -> "Pakyaw Charter"
                else -> "Standard Tricycle Ride"
            }

            val mode = if (rgRideMode.checkedRadioButtonId == R.id.rbKasya) {
                "Kasya kami riyan!"
            } else {
                "Pasabay lang po!"
            }

            val fareText = tvEstimatedFare.text.toString()

            startLookingForDriverSequence(pickup, dropoff, serviceType, mode, fareText)
        }
    }

    private fun startLookingForDriverSequence(pickup: String, dropoff: String, serviceType: String, mode: String, fare: String) {
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
            tvSub.text = "Connecting to nearest tricycle drivers..."
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
                    putExtra("SERVICE_TYPE", serviceType)
                    putExtra("BOOKING_MODE", mode)
                    putExtra("EST_FARE", fare)
                }
                startActivity(intent)
                finish()
            }, 1200)
        }, 7000)
    }
}
