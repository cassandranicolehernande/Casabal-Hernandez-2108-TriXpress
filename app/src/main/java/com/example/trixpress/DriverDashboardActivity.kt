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

        val btnAcceptRide = findViewById<Button?>(R.id.btnAcceptRide)
        val btnDeclineRide = findViewById<Button?>(R.id.btnDeclineRide)

        val tvDriverPickup = findViewById<TextView?>(R.id.tvDriverPickup)
        val tvDriverDestination = findViewById<TextView?>(R.id.tvDriverDestination)
        val tvDriverFare = findViewById<TextView?>(R.id.tvDriverFare)

        btnAcceptRide?.setOnClickListener {
            Toast.makeText(this, "Ride Accepted! Navigating to Pickup...", Toast.LENGTH_SHORT).show()
        }

        btnDeclineRide?.setOnClickListener {
            Toast.makeText(this, "Ride Declined", Toast.LENGTH_SHORT).show()
        }
    }
}
