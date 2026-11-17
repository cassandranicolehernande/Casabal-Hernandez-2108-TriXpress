package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etFeedback = findViewById<EditText>(R.id.etFeedback)
        val btnSubmitRating = findViewById<Button>(R.id.btnSubmitRating)

        btnSubmitRating.setOnClickListener {
            val rating = ratingBar.rating
            val feedback = etFeedback.text.toString().trim()

            Toast.makeText(this, "Salamat sa feedback mo! (${rating.toInt()} Stars) ⭐️", Toast.LENGTH_LONG).show()

            val intent = Intent(this, PassengerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
