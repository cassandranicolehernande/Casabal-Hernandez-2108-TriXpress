package com.example.trixpress

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SearchDropoffActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_dropoff)

        val pickupLocation = intent.getStringExtra("PICKUP_LOCATION") ?: "Nasugbu, Batangas"
        findViewById<TextView>(R.id.tvSearchPickup).text = pickupLocation

        val etDropoff = findViewById<EditText>(R.id.etSearchDropoff)
        val rgRideMode = findViewById<RadioGroup>(R.id.rgRideMode)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        btnConfirmBooking.setOnClickListener {
            val dropoff = etDropoff.text.toString().trim()

            if (dropoff.isEmpty()) {
                Toast.makeText(this, "Please enter a drop-off location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mode = if (rgRideMode.checkedRadioButtonId == R.id.rbKasya) {
                "Kasya kami riyan!"
            } else {
                "Pasabay lang po!"
            }

            startLookingForDriverSequence(pickupLocation, dropoff, mode)
        }
    }

    private fun startLookingForDriverSequence(pickup: String, dropoff: String, mode: String) {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val textView = dialogView.findViewById<TextView>(android.R.id.text1)
        textView.text = "Looking for drivers..."
        textView.textSize = 18f
        textView.gravity = android.view.Gravity.CENTER

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.show()

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({ textView.text = "Wait kalang konti..." }, 2000)
        handler.postDelayed({ textView.text = "Please wait lang..." }, 4000)
        handler.postDelayed({ textView.text = "Malapit na tayka..." }, 6000)

        handler.postDelayed({
            textView.text = "Eto na meron na!"
            handler.postDelayed({
                dialog.dismiss()
                // Redirect to Map/Tracking with Driver info and Pickup/Dropoff data
                val intent = Intent(this, TrackingActivity::class.java)
                intent.putExtra("DRIVER_NAME", "Kuya Roberto")
                intent.putExtra("PICKUP_LOC", pickup)
                intent.putExtra("DEST_LOC", dropoff)
                intent.putExtra("BOOKING_MODE", mode)
                intent.putExtra("EST_FARE", "₱50.00")
                startActivity(intent)
                finish()
            }, 1500)
        }, 8000)
    }
}