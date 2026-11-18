package com.example.trixpress

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

        // Additional labels in the layout if needed or dynamically updated
        val btnEditProfile = findViewById<Button?>(R.id.btnEditProfile)
        val btnTripHistory = findViewById<Button?>(R.id.btnTripHistory)
        val btnLogout = findViewById<Button?>(R.id.btnLogout)

        val btnNavDashboard = findViewById<Button?>(R.id.btnNavDashboard)
        val btnNavExplore = findViewById<Button?>(R.id.btnNavExplore)
        val btnNavHistory = findViewById<Button?>(R.id.btnNavHistory)

        // Check if user is a Driver or Passenger
        val isDriver = intent.getBooleanExtra("IS_DRIVER", false) || (intent.getStringExtra("USER_ROLE") == "DRIVER")

        if (isDriver) {
            // Driver Profile Info
            val driverName = intent.getStringExtra("DRIVER_NAME") ?: "Kuya Roberto (Driver)"
            val license = intent.getStringExtra("DRIVER_LICENSE") ?: "N12-34-567890"
            val toda = intent.getStringExtra("DRIVER_TODA") ?: "ARASOF Campus TODA"
            val plate = intent.getStringExtra("DRIVER_PLATE") ?: "AB-1234"
            val type = intent.getStringExtra("TRICYCLE_TYPE") ?: "Honda Tricycle"

            tvProfileName?.text = "🛺 $driverName"
            tvProfileContact?.text = "License: $license | Plate: $plate"
            tvProfileZone?.text = "TODA: $toda\nVehicle: $type"
            btnTripHistory?.text = "View Driver Earnings"
        } else {
            // Passenger Profile Info
            val name = intent.getStringExtra("USER_NAME") ?: "Passenger User"
            val phone = intent.getStringExtra("USER_PHONE") ?: "+63 912 345 6789"
            val address = intent.getStringExtra("USER_ADDRESS") ?: "Nasugbu, Batangas (Zone 1)"

            tvProfileName?.text = "👤 $name"
            tvProfileContact?.text = "Contact: $phone"
            tvProfileZone?.text = "Address / Zone: $address"
            btnTripHistory?.text = "View Recent Bookings"
        }

        btnEditProfile?.setOnClickListener {
            Toast.makeText(this, "Edit profile feature selected", Toast.LENGTH_SHORT).show()
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
