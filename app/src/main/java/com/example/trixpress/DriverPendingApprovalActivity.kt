package com.example.trixpress

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DriverPendingApprovalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_pending)

        val btnCheckStatus = findViewById<Button>(R.id.btnCheckStatus)

        btnCheckStatus.setOnClickListener {
            // Dito sinusuri kung aprubado na ng admin ang terms/rules
            // Para sa testing, direkta itong magbubukas ng Driver Dashboard kapag na-verify na
            Toast.makeText(this, "Application Approved! Redirecting to Dashboard...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, DriverDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}