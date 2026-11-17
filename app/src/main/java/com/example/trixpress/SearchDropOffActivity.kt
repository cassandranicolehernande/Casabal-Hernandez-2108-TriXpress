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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_dropoff)

        val etPickup = findViewById<AutoCompleteTextView>(R.id.etSearchPickup)
        val etDropoff = findViewById<AutoCompleteTextView>(R.id.etSearchDropoff)

        val adapter = ArrayAdapter(this, R.layout.item_dropdown_suggestion, nasugbuLandmarks)
        etPickup.setAdapter(adapter)
        etDropoff.setAdapter(adapter)

        // Custom glassmorphism dropdown background with oval-rectangle & yellow stroke
        etPickup.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)
        etDropoff.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)

        // Pre-fill default pickup if passed
        val defaultPickup = intent.getStringExtra("PICKUP_LOCATION") ?: "BatStateU ARASOF Campus, Nasugbu"
        etPickup.setText(defaultPickup, false)

        etPickup.setOnClickListener { etPickup.showDropDown() }
        etDropoff.setOnClickListener { etDropoff.showDropDown() }

        val rgRideMode = findViewById<RadioGroup>(R.id.rgRideMode)
        val rgServiceType = findViewById<RadioGroup>(R.id.rgServiceType)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        btnConfirmBooking.setOnClickListener {
            val pickup = etPickup.text.toString().trim()
            val dropoff = etDropoff.text.toString().trim()

            if (pickup.isEmpty()) {
                Toast.makeText(this, "Please enter your pick-up address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dropoff.isEmpty()) {
                Toast.makeText(this, "Please enter your drop-off address", Toast.LENGTH_SHORT).show()
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

            startLookingForDriverSequence(pickup, dropoff, serviceType, mode)
        }
    }

    private fun startLookingForDriverSequence(pickup: String, dropoff: String, serviceType: String, mode: String) {
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
            tvSub.text = "Estimated fare & route mapped via Google Maps."
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
                    putExtra("EST_FARE", "₱50.00")
                }
                startActivity(intent)
                finish()
            }, 1200)
        }, 7000)
    }
}
