package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val tvReceiptDriver = findViewById<TextView?>(R.id.tvReceiptDriver)
        val tvReceiptPickup = findViewById<TextView?>(R.id.tvReceiptPickup)
        val tvReceiptDestination = findViewById<TextView?>(R.id.tvReceiptDestination)
        val tvReceiptFare = findViewById<TextView?>(R.id.tvReceiptFare)

        val btnRateDriver = findViewById<Button?>(R.id.btnRateDriver)
        val btnBackToDashboard = findViewById<Button?>(R.id.btnBackToDashboard)

        // Retrieve extras passed from previous activity
        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Mario (Bunducan TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val fare = intent.getStringExtra("EST_FARE") ?: "₱50.00"

        // Set text values
        tvReceiptDriver?.text = driverName
        tvReceiptPickup?.text = pickupLoc
        tvReceiptDestination?.text = destLoc
        tvReceiptFare?.text = fare

        btnRateDriver?.setOnClickListener {
            Toast.makeText(this, "Thank you for rating your trip!", Toast.LENGTH_SHORT).show()
        }

        btnBackToDashboard?.setOnClickListener {
            val intent = Intent(this, PassengerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
