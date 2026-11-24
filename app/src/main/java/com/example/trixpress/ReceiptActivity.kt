package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

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
        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val fare = intent.getStringExtra("EST_FARE") ?: "₱50.00"
        val isDriver = intent.getBooleanExtra("IS_DRIVER", false)

        // Set text values
        tvReceiptDriver?.text = if (isDriver) "Passenger Booking Summary" else driverName
        tvReceiptPickup?.text = pickupLoc
        tvReceiptDestination?.text = destLoc
        tvReceiptFare?.text = fare

        if (isDriver) {
            btnRateDriver?.text = "🗺️ Open Exact Route in Google Maps App"
            btnBackToDashboard?.text = "✓ Complete Ride & Save to History"

            btnRateDriver?.setOnClickListener {
                // Launch official Google Maps app with passenger's exact pickup and drop-off
                val mapsUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(pickupLoc)}&destination=${Uri.encode(destLoc)}&travelmode=driving")
                val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri).setPackage("com.google.android.apps.maps")
                try {
                    startActivity(mapIntent)
                } catch (_: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
                }
            }

            btnBackToDashboard?.setOnClickListener {
                // Save completed ride to driver trip history
                try {
                    val prefs = getSharedPreferences("TriXpressHistory", Context.MODE_PRIVATE)
                    val existingStr = prefs.getString("SAVED_TRIPS", "[]") ?: "[]"
                    val jsonArray = JSONArray(existingStr)

                    val tripObj = JSONObject().apply {
                        put("driver", "Driver (You)")
                        put("pickup", pickupLoc)
                        put("dest", destLoc)
                        put("fare", fare)
                        put("rating", "5.0")
                    }
                    jsonArray.put(tripObj)
                    prefs.edit().putString("SAVED_TRIPS", jsonArray.toString()).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Toast.makeText(this, "Ride completed and saved to Recent Trip History!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, DriverDashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
        } else {
            btnRateDriver?.text = "⭐ Rate Driver"
            btnRateDriver?.setOnClickListener {
                val intent = Intent(this, RatingActivity::class.java).apply {
                    putExtra("DRIVER_NAME", driverName)
                    putExtra("PICKUP_LOC", pickupLoc)
                    putExtra("DEST_LOC", destLoc)
                    putExtra("EST_FARE", fare)
                }
                startActivity(intent)
            }

            btnBackToDashboard?.text = "🔙 Back to Dashboard"
            btnBackToDashboard?.setOnClickListener {
                val targetIntent = Intent(this, PassengerDashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(targetIntent)
                finish()
            }
        }
    }
}
