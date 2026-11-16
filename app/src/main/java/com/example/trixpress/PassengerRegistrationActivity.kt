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

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSubmitRegistration = findViewById<Button>(R.id.btnSubmitRegistration)

        val userPhone = intent.getStringExtra("USER_PHONE") ?: "+639000000000"

        btnSubmitRegistration.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val age = etAge.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (name.isEmpty() || age.isEmpty() || address.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill out all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_NAME", name)
                putExtra("USER_AGE", age)
                putExtra("USER_ADDRESS", address)
                putExtra("USER_EMAIL", email)
                putExtra("USER_PHONE", userPhone)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
