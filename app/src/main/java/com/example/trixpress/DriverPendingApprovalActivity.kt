package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DriverPendingApprovalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_pending)

        val prefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
        val driverStatus = prefs.getString("DRIVER_STATUS", "PENDING_VERIFICATION") ?: "PENDING_VERIFICATION"
        val driverName = intent.getStringExtra("DRIVER_NAME") ?: prefs.getString("DRIVER_NAME", "Driver") ?: "Driver"
        val driverContact = intent.getStringExtra("DRIVER_CONTACT") ?: prefs.getString("USER_PHONE", "+63 912 345 6789") ?: "+63 912 345 6789"
        val driverLicense = intent.getStringExtra("DRIVER_LICENSE") ?: prefs.getString("DRIVER_LICENSE", "N12-34-567890") ?: "N12-34-567890"
        val driverToda = intent.getStringExtra("DRIVER_TODA") ?: prefs.getString("DRIVER_TODA", "ARASOF Campus TODA") ?: "ARASOF Campus TODA"
        val driverPlate = intent.getStringExtra("DRIVER_PLATE") ?: prefs.getString("DRIVER_PLATE", "AB-1234") ?: "AB-1234"
        val tricycleType = intent.getStringExtra("TRICYCLE_TYPE") ?: prefs.getString("TRICYCLE_TYPE", "Standard Sidecar") ?: "Standard Sidecar"

        val tvApprovalStatus = findViewById<TextView>(R.id.tvApprovalStatus)
        val tvDriverSummary = findViewById<TextView>(R.id.tvDriverSummary)
        val btnCheckStatus = findViewById<Button>(R.id.btnCheckStatus)
        val adminSection = findViewById<LinearLayout>(R.id.adminReviewSection)
        val btnAcceptAdmin = findViewById<Button>(R.id.btnAcceptAdmin)
        val btnDeclineAdmin = findViewById<Button>(R.id.btnDeclineAdmin)
        val btnProceedDashboard = findViewById<Button>(R.id.btnProceedDashboard)

        val summaryText = "• Name: $driverName\n• Contact: $driverContact\n• License: $driverLicense\n• TODA: $driverToda\n• Unit: $tricycleType (Plate: $driverPlate)"
        tvDriverSummary?.text = summaryText

        if (driverStatus == "ACCEPTED & REGISTERED") {
            tvApprovalStatus.text = "ACCEPTED & REGISTERED ✅\nYour driver registration has been examined and approved by admin."
            btnCheckStatus.visibility = View.GONE
            adminSection.visibility = View.GONE
            btnProceedDashboard.visibility = View.VISIBLE
        } else {
            tvApprovalStatus.text = "PENDING VERIFICATION ⏳\nAn admin is currently examining your information, license, and TODA compliance."
            adminSection.visibility = View.VISIBLE
            btnProceedDashboard.visibility = View.GONE
        }

        btnCheckStatus.setOnClickListener {
            val currentStatus = prefs.getString("DRIVER_STATUS", "PENDING_VERIFICATION")
            if (currentStatus == "ACCEPTED & REGISTERED") {
                Toast.makeText(this, "Application Approved! Proceeding to Dashboard...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DriverDashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } else {
                Toast.makeText(this, "Status: Still Pending Admin Examination.", Toast.LENGTH_SHORT).show()
            }
        }

        btnAcceptAdmin.setOnClickListener {
            prefs.edit().putString("DRIVER_STATUS", "ACCEPTED & REGISTERED").apply()
            Toast.makeText(this, "Driver Accepted & Registered Successfully!", Toast.LENGTH_LONG).show()
            recreate()
        }

        btnDeclineAdmin.setOnClickListener {
            Toast.makeText(this, "Driver application declined.", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnProceedDashboard.setOnClickListener {
            val intent = Intent(this, DriverDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
