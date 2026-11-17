package com.example.trixpress

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class DriverRegistrationActivity : AppCompatActivity() {

    data class TricycleUnit(val type: String, val plate: String, val photoUri: String?)

    private val tricycleUnitsList = mutableListOf<TricycleUnit>()
    private var currentDriverPhotoUri: Uri? = null
    private var currentTricyclePhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_registration)

        val etLastName = findViewById<EditText>(R.id.etDriverLastName)
        val etFirstName = findViewById<EditText>(R.id.etDriverFirstName)
        val etMiddleName = findViewById<EditText>(R.id.etDriverMiddleName)
        val etLicense = findViewById<EditText>(R.id.etDriverLicense)

        val etType = findViewById<EditText>(R.id.etTricycleType)
        val etPlate = findViewById<EditText>(R.id.etPlateNumber)
        // FIXED: Changed from tvUnitHeader to tvUnitTitle to match XML
        val tvUnitTitle = findViewById<TextView>(R.id.tvUnitTitle)

        // FIXED: Changed IDs to match XML (btnUploadDriver2x2, tvDriver2x2Status)
        val btnUploadDriver2x2 = findViewById<Button>(R.id.btnUploadDriver2x2)
        val tvDriver2x2Status = findViewById<TextView>(R.id.tvDriver2x2Status)

        val btnUploadTricyclePic = findViewById<Button>(R.id.btnUploadTricyclePic)
        val tvTricyclePicStatus = findViewById<TextView>(R.id.tvTricyclePicStatus)

        val btnAddAnother = findViewById<Button>(R.id.btnAddAnotherUnit)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitDriverReg)

        // Activity Result Launcher para sa Driver Photo / 2x2 Picture
        val driverImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                currentDriverPhotoUri = uri
                tvDriver2x2Status.text = "2x2 Driver photo attached"
                Toast.makeText(this, "Driver photo selected!", Toast.LENGTH_SHORT).show()
            }
        }

        // Activity Result Launcher para sa Tricycle Photo
        val tricycleImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                currentTricyclePhotoUri = uri
                tvTricyclePicStatus.text = "Tricycle photo attached"
                Toast.makeText(this, "Tricycle photo selected!", Toast.LENGTH_SHORT).show()
            }
        }

        // FIXED: Updated variable name to match
        btnUploadDriver2x2.setOnClickListener {
            driverImageLauncher.launch("image/*")
        }

        btnUploadTricyclePic.setOnClickListener {
            tricycleImageLauncher.launch("image/*")
        }

        // Pagdagdag ng kasalukuyang tricycle unit (Support for multiple units)
        btnAddAnother.setOnClickListener {
            val type = etType.text.toString().trim()
            val plate = etPlate.text.toString().trim()

            if (type.isEmpty() || plate.isEmpty()) {
                Toast.makeText(this, "Please fill in the current unit's type and plate number first.", Toast.LENGTH_SHORT).show()
            } else {
                tricycleUnitsList.add(TricycleUnit(type, plate, currentTricyclePhotoUri?.toString()))
                Toast.makeText(this, "Unit saved! (${tricycleUnitsList.size} total unit/s)", Toast.LENGTH_SHORT).show()

                etType.text.clear()
                etPlate.text.clear()
                currentTricyclePhotoUri = null
                tvTricyclePicStatus.text = "No tricycle picture attached" // Updated to match your XML default text

                // FIXED: Updated variable name to match
                tvUnitTitle.text = "Step 2: Tricycle Details (Unit ${tricycleUnitsList.size + 1})"
            }
        }

        // Kumpletuhin ang Driver Registration at Dumiretso sa Admin Approval Screen
        btnSubmit.setOnClickListener {
            val lastName = etLastName.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()
            val license = etLicense.text.toString().trim()

            val lastType = etType.text.toString().trim()
            val lastPlate = etPlate.text.toString().trim()

            if (lastName.isEmpty() || firstName.isEmpty() || license.isEmpty()) {
                Toast.makeText(this, "Please complete your personal information and license.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If the user started filling the last unit, require BOTH fields
            if (lastType.isNotEmpty() || lastPlate.isNotEmpty()) {
                if (lastType.isEmpty() || lastPlate.isEmpty()) {
                    Toast.makeText(this, "Please complete the last unit's type and plate number.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                tricycleUnitsList.add(TricycleUnit(lastType, lastPlate, currentTricyclePhotoUri?.toString()))
            }

            if (tricycleUnitsList.isEmpty()) {
                Toast.makeText(this, "Please add at least one tricycle unit.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentDriverPhotoUri == null) {
                Toast.makeText(this, "Please upload the driver's 2x2 photo/ID.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Registration Submitted! Reviewing by Admin...", Toast.LENGTH_LONG).show()

            // Build full name including middle name (if present)
            val fullName = listOf(firstName, middleName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")

            val intent = Intent(this, DriverPendingApprovalActivity::class.java).apply {
                putExtra("DRIVER_NAME", fullName)
                putExtra("UNITS_COUNT", tricycleUnitsList.size)
                putExtra("DRIVER_PHOTO_URI", currentDriverPhotoUri.toString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}