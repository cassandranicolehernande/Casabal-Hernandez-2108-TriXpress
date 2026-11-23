package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvProfileName = findViewById<TextView?>(R.id.tvProfileName)
        val tvProfileContact = findViewById<TextView?>(R.id.tvProfileContact)
        val tvProfileZone = findViewById<TextView?>(R.id.tvProfileZone)

        val btnEditProfile = findViewById<Button?>(R.id.btnEditProfile)
        val btnTripHistory = findViewById<Button?>(R.id.btnTripHistory)
        val btnLogout = findViewById<Button?>(R.id.btnLogout)

        val btnNavDashboard = findViewById<Button?>(R.id.btnNavDashboard)
        val btnNavExplore = findViewById<Button?>(R.id.btnNavExplore)
        val btnNavHistory = findViewById<Button?>(R.id.btnNavHistory)

        // Read saved passenger registration info from SharedPreferences
        val prefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
        val savedRole = prefs.getString("USER_ROLE", "PASSENGER") ?: "PASSENGER"
        val savedName = intent.getStringExtra("USER_NAME") ?: prefs.getString("USER_NAME", "Passenger User") ?: "Passenger User"
        val savedPhone = intent.getStringExtra("USER_PHONE") ?: prefs.getString("USER_PHONE", "+63 912 345 6789") ?: "+63 912 345 6789"
        val savedAddress = intent.getStringExtra("USER_ADDRESS") ?: prefs.getString("USER_ADDRESS", "Nasugbu, Batangas (Zone 1)") ?: "Nasugbu, Batangas (Zone 1)"

        val isDriver = savedRole == "DRIVER" || intent.getBooleanExtra("IS_DRIVER", false)

        if (isDriver) {
            val driverName = prefs.getString("DRIVER_NAME", savedName) ?: savedName
            val license = prefs.getString("DRIVER_LICENSE", "N12-34-567890") ?: "N12-34-567890"
            val toda = prefs.getString("DRIVER_TODA", "ARASOF Campus TODA") ?: "ARASOF Campus TODA"
            val plate = prefs.getString("DRIVER_PLATE", "AB-1234") ?: "AB-1234"

            tvProfileName?.text = "🛺 $driverName"
            tvProfileContact?.text = "Contact: $savedPhone\nLicense: $license | Plate: $plate"
            tvProfileZone?.text = "TODA: $toda\nAddress: $savedAddress"
            btnTripHistory?.text = "View Driver Earnings"
        } else {
            tvProfileName?.text = "👤 $savedName"
            tvProfileContact?.text = "Contact: $savedPhone"
            tvProfileZone?.text = "Address / Zone: $savedAddress"
            btnTripHistory?.text = "View Recent Bookings"
        }

        btnEditProfile?.setOnClickListener {
            Toast.makeText(this, "Profile information updated", Toast.LENGTH_SHORT).show()
        }

        btnTripHistory?.setOnClickListener {
            if (isDriver) {
                Toast.makeText(this, "Total Earnings Today: ₱450.00 (8 Completed Trips)", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, TripHistoryActivity::class.java)
                startActivity(intent)
            }
        }

        btnLogout?.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnNavDashboard?.setOnClickListener {
            val intent = if (isDriver) {
                Intent(this, DriverDashboardActivity::class.java)
            } else {
                Intent(this, PassengerDashboardActivity::class.java)
            }
            startActivity(intent)
        }

        btnNavExplore?.setOnClickListener {
            val intent = Intent(this, ServiceZoneActivity::class.java)
            startActivity(intent)
        }

        btnNavHistory?.setOnClickListener {
            val intent = Intent(this, TripHistoryActivity::class.java)
            startActivity(intent)
        }
    }
}
