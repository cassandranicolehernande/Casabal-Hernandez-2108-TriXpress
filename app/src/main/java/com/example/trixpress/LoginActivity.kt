package com.example.trixpress

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Connect XML components
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val tvTrouble = findViewById<TextView>(R.id.tvTrouble)
        val tvEmailLogin = findViewById<TextView>(R.id.tvEmailLogin)

        // Underline Trouble Receiving Code
        tvTrouble.paintFlags =
            tvTrouble.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // LOGIN BUTTON
        btnLogin.setOnClickListener {

            val inputPhone = etPhone.text.toString().trim()

            // Remove spaces, dashes, and other symbols
            var phoneNumber = inputPhone.filter { it.isDigit() }

            // Handle numbers starting with 63
            if (phoneNumber.startsWith("63")) {
                phoneNumber = phoneNumber.substring(2)
            }

            // Handle numbers starting with 0
            if (phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.substring(1)
            }

            // Validate Philippine mobile number
            if (phoneNumber.matches(Regex("^9\\d{9}$"))) {

                // Format number with Philippine country code
                val formattedNumber = "+63$phoneNumber"

                Toast.makeText(
                    this,
                    "Phone number accepted!",
                    Toast.LENGTH_SHORT
                ).show()

                // Proceed to Passenger Registration
                val intent = Intent(
                    this,
                    PassengerRegistrationActivity::class.java
                )

                // Pass phone number to next activity
                intent.putExtra("USER_PHONE", formattedNumber)

                startActivity(intent)
                finish()

            } else {

                // Invalid phone number
                etPhone.error = "Enter a valid Philippine mobile number"

                Toast.makeText(
                    this,
                    "Please enter a valid 11-digit Philippine mobile number.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // TROUBLE RECEIVING CODE
        tvTrouble.setOnClickListener {

            Toast.makeText(
                this,
                "Please check your mobile number and network connection.",
                Toast.LENGTH_LONG
            ).show()
        }

        // EMAIL LOGIN
        tvEmailLogin.setOnClickListener {

            Toast.makeText(
                this,
                "Email login is not available yet.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
