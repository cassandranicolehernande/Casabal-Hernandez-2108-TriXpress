package com.example.trixpress

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TripHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_history)

        val btnBackFromHistory = findViewById<Button?>(R.id.btnBackFromHistory)

        btnBackFromHistory?.setOnClickListener {
            finish()
        }
    }
}
