package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DriverDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        val btnAcceptRide = findViewById<Button>(R.id.btnAcceptRide)
        val btnDeclineRide = findViewById<Button>(R.id.btnDeclineRide)
        val btnDriverProfile = findViewById<Button>(R.id.btnDriverProfile)

        val tvDriverPickup = findViewById<TextView>(R.id.tvDriverPickup)
        val tvDriverDestination = findViewById<TextView>(R.id.tvDriverDestination)
        val tvDriverFare = findViewById<TextView>(R.id.tvDriverFare)

        // Load driver info without TODA restriction (drivers can accept bookings anywhere in Nasugbu)
        val prefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
        val driverName = intent.getStringExtra("DRIVER_NAME") ?: prefs.getString("DRIVER_NAME", "Kuya Roberto") ?: "Kuya Roberto"

        // Load active booking from shared store (Single Source of Truth)
        val activePrefs = getSharedPreferences("TriXpressActiveBooking", Context.MODE_PRIVATE)
        val hasBooking = activePrefs.getBoolean("hasBooking", false)

        val pickupLoc = if (hasBooking) activePrefs.getString("pickupLoc", "BatStateU ARASOF Campus") else "BatStateU ARASOF - Nasugbu Campus"
        val destLoc = if (hasBooking) activePrefs.getString("destLoc", "Nasugbu Public Market") else "Nasugbu Public Market"
        val estFare = if (hasBooking) activePrefs.getString("fare", "₱50.00") else "₱50.00"
        val serviceType = if (hasBooking) activePrefs.getString("serviceType", "Individual Ride") else "Individual Ride"
        val mode = if (hasBooking) activePrefs.getString("bookingMode", "Individual Passenger") else "Individual Passenger"
        val notes = if (hasBooking) activePrefs.getString("notes", "") else ""
        val pickupLat = if (hasBooking) activePrefs.getFloat("pickupLat", 14.0673f).toDouble() else 14.0673
        val pickupLng = if (hasBooking) activePrefs.getFloat("pickupLng", 120.6262f).toDouble() else 120.6262
        val destLat = if (hasBooking) activePrefs.getFloat("destLat", 14.0754f).toDouble() else 14.0754
        val destLng = if (hasBooking) activePrefs.getFloat("destLng", 120.6358f).toDouble() else 120.6358

        btnDriverProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("IS_DRIVER", true)
                putExtra("DRIVER_NAME", driverName)
            }
            startActivity(intent)
        }

        // Display booking details unrestricted by TODA zone
        tvDriverPickup.text = "Passenger: Juan Dela Cruz ($serviceType)\nPickup: $pickupLoc"
        tvDriverDestination.text = "Drop-off: $destLoc\nMode: $mode • Note: ${notes?.ifEmpty { "None" } ?: "None"}"
        tvDriverFare.text = "Est. Fare: $estFare"

        btnAcceptRide.setOnClickListener {
            Toast.makeText(this, "Booking Accepted! Opening Trip Summary & Receipt...", Toast.LENGTH_SHORT).show()

            // Navigate directly to Receipt / Active Trip summary (No Post-Accept Map)
            val intent = Intent(this, ReceiptActivity::class.java).apply {
                putExtra("DRIVER_NAME", driverName)
                putExtra("PICKUP_LOC", pickupLoc)
                putExtra("DEST_LOC", destLoc)
                putExtra("EST_FARE", estFare)
                putExtra("PICKUP_LAT", pickupLat)
                putExtra("PICKUP_LNG", pickupLng)
                putExtra("DEST_LAT", destLat)
                putExtra("DEST_LNG", destLng)
                putExtra("IS_DRIVER", true)
            }
            startActivity(intent)
            finish()
        }

        btnDeclineRide.setOnClickListener {
            Toast.makeText(this, "Booking declined. Scanning Nasugbu zones for next passenger...", Toast.LENGTH_SHORT).show()
            tvDriverPickup.text = "Status: Scanning Nasugbu zones for passengers..."
            tvDriverDestination.text = "Drop-off: None"
            tvDriverFare.text = "Est. Fare: ₱0.00"
            btnAcceptRide.visibility = View.GONE
            btnDeclineRide.visibility = View.GONE
        }
    }
}
