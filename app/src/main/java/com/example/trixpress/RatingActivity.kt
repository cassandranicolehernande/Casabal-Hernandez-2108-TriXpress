package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class RatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etFeedback = findViewById<EditText>(R.id.etFeedback)
        val btnSubmitRating = findViewById<Button>(R.id.btnSubmitRating)

        val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (ARASOF TODA)"
        val pickupLoc = intent.getStringExtra("PICKUP_LOC") ?: "BatStateU ARASOF Campus"
        val destLoc = intent.getStringExtra("DEST_LOC") ?: "Nasugbu Public Market"
        val fare = intent.getStringExtra("EST_FARE") ?: "₱50.00"

        btnSubmitRating.setOnClickListener {
            val rating = ratingBar.rating

            // Save completed trip to Trip History SharedPreferences
            try {
                val prefs = getSharedPreferences("TriXpressHistory", Context.MODE_PRIVATE)
                val existingStr = prefs.getString("SAVED_TRIPS", "[]") ?: "[]"
                val jsonArray = JSONArray(existingStr)

                val tripObj = JSONObject().apply {
                    put("driver", driverName)
                    put("pickup", pickupLoc)
                    put("dest", destLoc)
                    put("fare", fare)
                    put("rating", String.format("%.1f", if (rating > 0) rating else 5.0f))
                }
                jsonArray.put(tripObj)

                prefs.edit().putString("SAVED_TRIPS", jsonArray.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Toast.makeText(this, "Salamat sa feedback mo! Saved to Trip History. ⭐️", Toast.LENGTH_LONG).show()

            val intent = Intent(this, PassengerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
