package com.example.trixpress

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

            Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show()

            // Go directly to the Passenger Dashboard (Map)
            val intent = Intent(this, PassengerDashboardActivity::class.java).apply {
                putExtra("USER_NAME", "$firstName $lastName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}