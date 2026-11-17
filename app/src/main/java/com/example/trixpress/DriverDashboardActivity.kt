package com.example.trixpress

import android.content.Intent
import android.os.Bundle
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

        val tvDriverPickup = findViewById<TextView>(R.id.tvDriverPickup)
        val tvDriverDestination = findViewById<TextView>(R.id.tvDriverDestination)
        val tvDriverFare = findViewById<TextView>(R.id.tvDriverFare)

        // Simulate an incoming booking
        tvDriverPickup.text = "Passenger: Juan Dela Cruz\nPickup: BatStateU ARASOF"
        tvDriverDestination.text = "Drop-off: Nasugbu Market\nMode: Pasabay lang po! (1 Pax)"
        tvDriverFare.text = "Est. Fare: ₱50.00"

        btnAcceptRide.setOnClickListener {
            Toast.makeText(this, "Ride Accepted! Navigating to Passenger...", Toast.LENGTH_SHORT).show()

            // Driver goes to the map to pick up the passenger
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("DRIVER_NAME", "You (Kuya Driver)")
            intent.putExtra("PICKUP_LOC", "BatStateU ARASOF")
            intent.putExtra("DEST_LOC", "Nasugbu Market")
            startActivity(intent)
        }

        btnDeclineRide.setOnClickListener {
            Toast.makeText(this, "Ride Declined", Toast.LENGTH_SHORT).show()
            // Here you would hide the card and wait for the next booking
        }
    }
}