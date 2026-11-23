package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class DriverRegistrationActivity : AppCompatActivity() {

    private var currentDriverPhotoUri: Uri? = null
    private var currentTricyclePhotoUri: Uri? = null

    private val nasugbuTodas = arrayOf(
        "ARASOF Campus TODA",
        "Town Proper TODA",
        "Town Plaza TODA",
        "Nasugbu Public Market TODA",
        "Bunducan TODA",
        "Wawa Port TODA",
        "Bucana TODA",
        "Natipuan TODA",
        "Calayo TODA",
        "Kaylaway TODA"
    )

    private val tricycleModels = arrayOf(
        "Bajaj RE (4 Seater)",
        "Honda TMX 125 (3 Seater)",
        "Honda TMX 150 Supremo (3 Seater)",
        "Kawasaki Barako II (3 Seater)",
        "Yamaha Sight 115 (3 Seater)",
        "TVS King Deluxe (4 Seater)",
        "Suzuki GD110 (3 Seater)",
        "Rusi Trimoto (4 Seater)",
        "E-Trike Passenger Unit (4 Seater)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_registration)

        val etLastName = findViewById<EditText>(R.id.etDriverLastName)
        val etFirstName = findViewById<EditText>(R.id.etDriverFirstName)
        val etMiddleName = findViewById<EditText>(R.id.etDriverMiddleName)
        val etContact = findViewById<EditText>(R.id.etDriverContact)
        val etToda = findViewById<AutoCompleteTextView>(R.id.etTodaAssociation)
        val etZone = findViewById<EditText>(R.id.etRegisteredZone)
        val etLicense = findViewById<EditText>(R.id.etDriverLicense)

        val etType = findViewById<AutoCompleteTextView>(R.id.etTricycleType)
        val etSeater = findViewById<EditText>(R.id.etSeaterCapacity)
        val etPlate = findViewById<EditText>(R.id.etPlateNumber)

        val iv2x2Preview = findViewById<ImageView>(R.id.iv2x2Preview)
        val btnUploadDriver2x2 = findViewById<Button>(R.id.btnUploadDriver2x2)
        val tvDriver2x2Status = findViewById<TextView>(R.id.tvDriver2x2Status)

        val ivTrikePhotoPreview = findViewById<ImageView>(R.id.ivTrikePhotoPreview)
        val btnUploadTricyclePic = findViewById<Button>(R.id.btnUploadTricyclePic)
        val tvTricyclePicStatus = findViewById<TextView>(R.id.tvTricyclePicStatus)

        val btnSubmit = findViewById<Button>(R.id.btnSubmitDriverReg)

        // Setup AutoComplete for TODA Associations
        val todaAdapter = ArrayAdapter(this, R.layout.item_dropdown_suggestion, nasugbuTodas)
        etToda.setAdapter(todaAdapter)
        etToda.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)
        etToda.setOnClickListener { etToda.showDropDown() }

        // Setup AutoComplete for Tricycle Models & Seater Detection
        val typeAdapter = ArrayAdapter(this, R.layout.item_dropdown_suggestion, tricycleModels)
        etType.setAdapter(typeAdapter)
        etType.setDropDownBackgroundResource(R.drawable.bg_dropdown_item)
        etType.setOnClickListener { etType.showDropDown() }

        etType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().lowercase()
                when {
                    input.contains("bajaj") || input.contains("re") || input.contains("tvs") || input.contains("king") || input.contains("e-trike") || input.contains("rusi") -> {
                        etSeater.setText("4 Seater Unit")
                    }
                    input.contains("honda") || input.contains("tmx") || input.contains("barako") || input.contains("kawasaki") || input.contains("yamaha") || input.contains("suzuki") -> {
                        etSeater.setText("3 Seater Standard Unit")
                    }
                    else -> {
                        if (input.isNotEmpty()) etSeater.setText("3-4 Seater Unit")
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Driver 2x2 Photo Image Launcher with Visible Preview
        val driverImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                currentDriverPhotoUri = uri
                iv2x2Preview.setImageURI(uri)
                iv2x2Preview.visibility = View.VISIBLE
                tvDriver2x2Status.text = "📷 2x2 Driver photo attached and verified"
                Toast.makeText(this, "2x2 Driver Photo Attached!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tricycle Photo Image Launcher with Visible Preview
        val tricycleImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                currentTricyclePhotoUri = uri
                ivTrikePhotoPreview.setImageURI(uri)
                ivTrikePhotoPreview.visibility = View.VISIBLE
                tvTricyclePicStatus.text = "🛺 Tricycle unit photo attached and verified"
                Toast.makeText(this, "Tricycle Unit Photo Attached!", Toast.LENGTH_SHORT).show()
            }
        }

        btnUploadDriver2x2.setOnClickListener { driverImageLauncher.launch("image/*") }
        btnUploadTricyclePic.setOnClickListener { tricycleImageLauncher.launch("image/*") }

        // Complete Registration & Save Profile
        btnSubmit.setOnClickListener {
            val lastName = etLastName.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()
            val contact = etContact.text.toString().trim()
            val toda = etToda.text.toString().trim()
            val zone = etZone.text.toString().trim().ifEmpty { "Nasugbu Zone 1" }
            val license = etLicense.text.toString().trim()
            val trikeType = etType.text.toString().trim()
            val plate = etPlate.text.toString().trim()

            if (lastName.isEmpty() || firstName.isEmpty() || contact.isEmpty() || license.isEmpty() || toda.isEmpty()) {
                Toast.makeText(this, "Please complete all required fields including contact, TODA, and license.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (trikeType.isEmpty() || plate.isEmpty()) {
                Toast.makeText(this, "Please complete your tricycle model and plate number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = listOf(firstName, middleName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")

            // Save Driver Registration info locally into SharedPreferences
            val prefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("IS_LOGGED_IN", true)
                putString("USER_ROLE", "DRIVER")
                putString("USER_NAME", fullName)
                putString("DRIVER_NAME", fullName)
                putString("USER_PHONE", contact)
                putString("DRIVER_LICENSE", license)
                putString("DRIVER_TODA", toda)
                putString("DRIVER_PLATE", plate)
                putString("TRICYCLE_TYPE", trikeType)
                putString("USER_ADDRESS", zone)
                apply()
            }

            Toast.makeText(this, "Driver Profile Created & Registration Complete!", Toast.LENGTH_LONG).show()

            val intent = Intent(this, DriverDashboardActivity::class.java).apply {
                putExtra("DRIVER_NAME", fullName)
                putExtra("DRIVER_TODA", toda)
                putExtra("DRIVER_PLATE", plate)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
