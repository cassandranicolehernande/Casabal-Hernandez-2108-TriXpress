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

        // Display user defaults
        tvProfileName?.text = "Nasugbu Passenger"
        tvProfileContact?.text = "+63 912 345 6789"
        tvProfileZone?.text = "ARASOF Campus / Town Proper Zone"

        btnEditProfile?.setOnClickListener {
            Toast.makeText(this, "Edit profile feature selected", Toast.LENGTH_SHORT).show()
        }

        btnTripHistory?.setOnClickListener {
            Toast.makeText(this, "Opening Recent Bookings History", Toast.LENGTH_SHORT).show()
        }

        btnLogout?.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
