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

        val btnEditProfile = findViewById<Button?>(R.id.btnEditProfile)
        val btnTripHistory = findViewById<Button?>(R.id.btnTripHistory)
        val btnLogout = findViewById<Button?>(R.id.btnLogout)

        // --- IDagdag ang mga ito: Bottom Navigation Buttons ---
        val btnNavDashboard = findViewById<Button?>(R.id.btnNavDashboard)
        val btnNavExplore = findViewById<Button?>(R.id.btnNavExplore)
        val btnNavHistory = findViewById<Button?>(R.id.btnNavHistory)

        // Kunin ang mga in-input at ipinasa mula sa PassengerRegistrationActivity
        val name = intent.getStringExtra("USER_NAME") ?: "Nasugbu Passenger"
        val phone = intent.getStringExtra("USER_PHONE") ?: "+63 912 345 6789"
        val age = intent.getStringExtra("USER_AGE") ?: ""
        val address = intent.getStringExtra("USER_ADDRESS") ?: "ARASOF Campus / Town Proper Zone"
        val email = intent.getStringExtra("USER_EMAIL") ?: ""

        // I-display ang totoong impormasyon sa mga TextViews
        tvProfileName?.text = name
        tvProfileContact?.text = if (email.isNotEmpty()) "$phone | $email" else phone
        tvProfileZone?.text = if (age.isNotEmpty()) "$address (Age: $age)" else address

        btnEditProfile?.setOnClickListener {
            Toast.makeText(this, "Edit profile feature selected", Toast.LENGTH_SHORT).show()
        }

        btnTripHistory?.setOnClickListener {
            val intent = Intent(this, TripHistoryActivity::class.java)
            startActivity(intent)
        }

        btnLogout?.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // --- IDagdag ang mga ito: Click Listeners para sa Bottom Navigation ---
        btnNavDashboard?.setOnClickListener {
            val intent = Intent(this, PassengerDashboardActivity::class.java)
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
