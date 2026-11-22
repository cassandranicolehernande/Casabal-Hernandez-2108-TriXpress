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

        // Simulate an incoming booking request
        tvDriverPickup.text = "Passenger: Juan Dela Cruz\nPickup: BatStateU ARASOF Campus"
        tvDriverDestination.text = "Drop-off: Nasugbu Public Market\nMode: Pasabay lang po! (1 Pax)"
        tvDriverFare.text = "Est. Fare: ₱50.00"

        btnAcceptRide.setOnClickListener {
            Toast.makeText(this, "Ride Accepted! Navigating to Passenger...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, TrackingActivity::class.java).apply {
                putExtra("DRIVER_NAME", "You (Kuya Driver)")
                putExtra("PICKUP_LOC", "BatStateU ARASOF Campus")
                putExtra("DEST_LOC", "Nasugbu Public Market")
                putExtra("IS_DRIVER", true)
            }
            startActivity(intent)
        }

        btnDeclineRide.setOnClickListener {
            Toast.makeText(this, "Ride Declined. Waiting for next dispatch...", Toast.LENGTH_SHORT).show()
        }
    }
}
