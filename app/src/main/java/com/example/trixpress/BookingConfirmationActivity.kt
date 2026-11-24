package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class BookingConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_confirmation)

        val tvDriverName = findViewById<TextView?>(R.id.tvDriverName)
        val tvPickup = findViewById<TextView?>(R.id.tvPickup)
        val tvDestination = findViewById<TextView?>(R.id.tvDestination)
        val tvFare = findViewById<TextView?>(R.id.tvFare)
        val btnContinue = findViewById<Button?>(R.id.btnContinue)
        val btnCancelBooking = findViewById<Button?>(R.id.btnCancelBooking)

        // Retrieve extras passed from previous activity
        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val estFare = intent.getStringExtra("EST_FARE") ?: "₱50.00"
        val serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "Individual Ride"
        val bookingMode = intent.getStringExtra("BOOKING_MODE") ?: "Pasabay lang po!"
        val notes = intent.getStringExtra("DRIVER_NOTES") ?: ""
        val parcelUri = intent.getStringExtra("PARCEL_IMAGE_URI")
        val pLat = intent.getDoubleExtra("PICKUP_LAT", 14.0673)
        val pLng = intent.getDoubleExtra("PICKUP_LNG", 120.6262)
        val dLat = intent.getDoubleExtra("DEST_LAT", 14.0754)
        val dLng = intent.getDoubleExtra("DEST_LNG", 120.6358)

        // Set text values
        tvDriverName?.text = driverName
        tvPickup?.text = pickupLoc
        tvDestination?.text = destLoc
        tvFare?.text = estFare

        Toast.makeText(this, "Booking Confirmed for $driverName", Toast.LENGTH_SHORT).show()

        btnContinue?.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java).apply {
                putExtra("DRIVER_NAME", driverName)
                putExtra("PICKUP_LOC", pickupLoc)
                putExtra("DEST_LOC", destLoc)
                putExtra("EST_FARE", estFare)
                putExtra("SERVICE_TYPE", serviceType)
                putExtra("BOOKING_MODE", bookingMode)
                putExtra("DRIVER_NOTES", notes)
                putExtra("PARCEL_IMAGE_URI", parcelUri)
                putExtra("PICKUP_LAT", pLat)
                putExtra("PICKUP_LNG", pLng)
                putExtra("DEST_LAT", dLat)
                putExtra("DEST_LNG", dLng)
            }
            startActivity(intent)
        }

        btnCancelBooking?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancel Booking?")
                .setMessage("Are you sure you want to cancel this booking with $driverName?")
                .setPositiveButton("Yes, Cancel Booking") { _, _ ->
                    // Clear active booking from shared store
                    val activePrefs = getSharedPreferences("TriXpressActiveBooking", Context.MODE_PRIVATE)
                    activePrefs.edit().clear().apply()

                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PassengerDashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Keep Ride") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
