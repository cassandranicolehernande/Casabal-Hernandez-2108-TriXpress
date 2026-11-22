package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PassengerRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_registration)

        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etMiddleName = findViewById<EditText>(R.id.etMiddleName)
        val etContact = findViewById<EditText>(R.id.etContact)
        val btnSubmitRegistration = findViewById<Button>(R.id.btnSubmitRegistration)

        btnSubmitRegistration.setOnClickListener {
            val lastName = etLastName.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val contact = etContact.text.toString().trim()

            if (lastName.isEmpty() || firstName.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill out all required details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = "$firstName $lastName"

            // Save passenger account locally in SharedPreferences
            val prefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("IS_LOGGED_IN", true)
                putString("USER_ROLE", "PASSENGER")
                putString("USER_NAME", fullName)
                putString("USER_PHONE", contact)
                putString("USER_ADDRESS", "Nasugbu, Batangas (Zone 1)")
                apply()
            }

            Toast.makeText(this, "Account Saved & Registration Complete!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, PassengerDashboardActivity::class.java).apply {
                putExtra("USER_NAME", fullName)
                putExtra("USER_PHONE", contact)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
