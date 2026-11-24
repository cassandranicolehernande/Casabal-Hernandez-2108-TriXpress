package com.example.trixpress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class TripHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_history)

        val btnBackFromHistory = findViewById<Button?>(R.id.btnBackFromHistory)
        val historyContainer = findViewById<LinearLayout?>(R.id.historyContainer)

        btnBackFromHistory?.setOnClickListener {
            finish()
        }

        // Determine if current logged-in user is a Driver or Passenger
        val authPrefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
        val userRole = authPrefs.getString("USER_ROLE", "PASSENGER") ?: "PASSENGER"
        val isDriver = userRole == "DRIVER" || intent.getBooleanExtra("IS_DRIVER", false)

        // Use separate trip history stores for drivers vs passengers
        val historyPrefName = if (isDriver) "DriverTripHistory" else "PassengerTripHistory"
        val prefs = getSharedPreferences(historyPrefName, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("SAVED_TRIPS", "[]") ?: "[]"

        try {
            val jsonArray = JSONArray(jsonStr)
            if (jsonArray.length() > 0 && historyContainer != null) {
                historyContainer.removeAllViews()
                for (i in jsonArray.length() - 1 downTo 0) {
                    val obj = jsonArray.getJSONObject(i)
                    val driver = obj.optString("driver", "Kuya Roberto")
                    val pickup = obj.optString("pickup", "Pickup")
                    val dest = obj.optString("dest", "Destination")
                    val fare = obj.optString("fare", "₱50.00")
                    val rating = obj.optString("rating", "5.0")

                    val cardView = LayoutInflater.from(this).inflate(R.layout.item_trip_history_card, historyContainer, false)
                    cardView.findViewById<TextView>(R.id.tvHistoryDriver).text = "Completed • $driver"
                    cardView.findViewById<TextView>(R.id.tvHistoryFare).text = fare
                    cardView.findViewById<TextView>(R.id.tvHistoryRoute).text = "$pickup ➔ $dest"
                    cardView.findViewById<TextView>(R.id.tvHistoryRating).text = "★★★★★ ($rating Stars)"

                    historyContainer.addView(cardView)
                }
            } else if (historyContainer != null) {
                historyContainer.removeAllViews()
                val emptyTextView = TextView(this).apply {
                    text = if (isDriver) "No completed driver trips yet.\nAccepted rides will appear here." else "No completed trips yet.\nBook a ride with TriXpress to see your trip history!"
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 15f
                    gravity = Gravity.CENTER
                    setPadding(32, 64, 32, 32)
                }
                historyContainer.addView(emptyTextView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
