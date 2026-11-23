package com.example.trixpress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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

        // Load saved trips dynamically from SharedPreferences
        val prefs = getSharedPreferences("TriXpressHistory", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("SAVED_TRIPS", "[]") ?: "[]"

        try {
            val jsonArray = JSONArray(jsonStr)
            if (jsonArray.length() > 0 && historyContainer != null) {
                historyContainer.removeAllViews()
                for (i in jsonArray.length() - 1 downTo 0) {
                    val obj = jsonArray.getJSONObject(i)
                    val driver = obj.optString("driver", "Kuya Roberto (ARASOF TODA)")
                    val pickup = obj.optString("pickup", "BatStateU ARASOF Campus")
                    val dest = obj.optString("dest", "Nasugbu Public Market")
                    val fare = obj.optString("fare", "₱50.00")
                    val rating = obj.optString("rating", "5.0")

                    val cardView = LayoutInflater.from(this).inflate(R.layout.item_trip_history_card, historyContainer, false)
                    cardView.findViewById<TextView>(R.id.tvHistoryDriver).text = "Past Ride • $driver"
                    cardView.findViewById<TextView>(R.id.tvHistoryFare).text = fare
                    cardView.findViewById<TextView>(R.id.tvHistoryRoute).text = "$pickup ➔ $dest"
                    cardView.findViewById<TextView>(R.id.tvHistoryRating).text = "★★★★★ ($rating Stars)"

                    historyContainer.addView(cardView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
