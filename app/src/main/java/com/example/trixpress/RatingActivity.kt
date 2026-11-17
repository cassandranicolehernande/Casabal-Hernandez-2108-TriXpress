package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        val etFeedback = findViewById<EditText>(R.id.etFeedback)
        val btnSubmitRating = findViewById<Button>(R.id.btnSubmitRating)

        btnSubmitRating.setOnClickListener {
            val feedback = etFeedback.text.toString()
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()

            val intent = Intent(this, PassengerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}