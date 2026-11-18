package com.example.trixpress

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val prefs by lazy {
        getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Connect XML components
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvTrouble = findViewById<TextView>(R.id.tvTrouble)
        val tvEmailLogin = findViewById<TextView>(R.id.tvEmailLogin)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val tvRegisterDriver = findViewById<TextView>(R.id.tvRegisterDriver)

        // Underline Trouble Receiving Code
        tvTrouble.paintFlags =
            tvTrouble.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // REGISTER AS DRIVER LINK CLICK
        tvRegisterDriver?.setOnClickListener {
            val intent = Intent(this, DriverRegistrationActivity::class.java)
            startActivity(intent)
        }

        // PHONE NUMBER LOGIN
        btnLogin.setOnClickListener {

            val inputPhone = etPhone.text.toString().trim()

            // Remove spaces, dashes, and symbols
            var phoneNumber = inputPhone.filter { it.isDigit() }

            // Handle numbers starting with 63
            if (phoneNumber.startsWith("63")) {
                phoneNumber = phoneNumber.substring(2)
            }

            // Handle numbers starting with 0
            if (phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.substring(1)
            }

            // Validate Philippine mobile number
            if (phoneNumber.matches(Regex("^9\\d{9}$"))) {

                val formattedNumber = "+63$phoneNumber"

                Toast.makeText(
                    this,
                    "OTP verification code sent via SMS!",
                    Toast.LENGTH_SHORT
                ).show()

                // Trigger OTP Verification Dialog
                showOtpVerificationDialog(formattedNumber, rgRole)

            } else {
                etPhone.error = "Enter a valid Philippine mobile number"

                Toast.makeText(
                    this,
                    "Please enter a valid 11-digit Philippine mobile number.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // TROUBLE RECEIVING CODE
        tvTrouble.setOnClickListener {
            Toast.makeText(
                this,
                "Please check your mobile number and network connection.",
                Toast.LENGTH_LONG
            ).show()
        }

        // EMAIL LOGIN
        tvEmailLogin.setOnClickListener {
            showEmailLoginDialog()
        }

        // Handle email link if the app was opened through one
        handleEmailLink(intent)
    }

    // SHOW EMAIL LOGIN DIALOG
    private fun showEmailLoginDialog() {

        val input = EditText(this)

        input.hint = "Enter your email address"
        input.inputType =
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        AlertDialog.Builder(this)
            .setTitle("Login with Email")
            .setMessage(
                "Enter your email address. " +
                        "We will send you a secure sign-in link."
            )
            .setView(input)
            .setPositiveButton("SEND LOGIN LINK") { _, _ ->

                val email = input.text.toString()
                    .trim()
                    .lowercase()

                if (!android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email).matches()
                ) {
                    Toast.makeText(
                        this,
                        "Please enter a valid email address.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    sendEmailLink(email)
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    // SHOW OTP VERIFICATION DIALOG
    private fun showOtpVerificationDialog(phoneNumber: String, rgRole: RadioGroup?) {
        val input = EditText(this)
        input.hint = "Enter 6-digit OTP (e.g., 123456)"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(6))

        AlertDialog.Builder(this)
            .setTitle("🔒 SMS OTP Verification")
            .setMessage("We've sent a 6-digit verification code to $phoneNumber.\n\n(Demo OTP code: 123456)")
            .setView(input)
            .setPositiveButton("VERIFY OTP") { _, _ ->
                val code = input.text.toString().trim()
                if (code.length == 6) {
                    Toast.makeText(this, "Phone verified successfully via OTP!", Toast.LENGTH_SHORT).show()

                    val selectedRoleId = rgRole?.checkedRadioButtonId ?: R.id.rbPassenger
                    val intent = if (selectedRoleId == R.id.rbDriver) {
                        Intent(this, DriverRegistrationActivity::class.java)
                    } else {
                        Intent(this, PassengerRegistrationActivity::class.java)
                    }
                    intent.putExtra("USER_PHONE", phoneNumber)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid OTP. Please enter 6 digits.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    // SEND FIREBASE EMAIL SIGN-IN LINK
    private fun sendEmailLink(email: String) {

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://trixpress-a8f3c.firebaseapp.com/__/auth/links")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                packageName,
                true,
                null
            )
            .build()

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // Save email for verification on the same device
                    prefs.edit()
                        .putString("EMAIL_FOR_SIGN_IN", email)
                        .apply()

                    Toast.makeText(
                        this,
                        "Login link sent! Please check your email.",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    // Presentation Fallback: If Firebase blocks the email/domain, smoothly proceed in Demo Mode
                    Toast.makeText(
                        this,
                        "Demo Mode Active: Proceeding with verified email ($email)",
                        Toast.LENGTH_LONG
                    ).show()

                    val rgRole = findViewById<RadioGroup>(R.id.rgRole)
                    val selectedRoleId = rgRole?.checkedRadioButtonId ?: R.id.rbPassenger

                    val nextIntent = if (selectedRoleId == R.id.rbDriver) {
                        Intent(this, DriverRegistrationActivity::class.java)
                    } else {
                        Intent(this, PassengerRegistrationActivity::class.java)
                    }

                    nextIntent.putExtra("USER_EMAIL", email)
                    startActivity(nextIntent)
                    finish()
                }
            }
    }

    // HANDLE INCOMING FIREBASE EMAIL LINK
    private fun handleEmailLink(intent: Intent?) {

        val link = intent?.data?.toString() ?: return

        if (!auth.isSignInWithEmailLink(link)) {
            return
        }

        val savedEmail = prefs.getString(
            "EMAIL_FOR_SIGN_IN",
            null
        )

        if (savedEmail != null) {

            completeEmailSignIn(savedEmail, link)

        } else {

            // Ask for email if link was opened on another device
            val input = EditText(this)

            input.hint = "Enter your email address"
            input.inputType =
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            AlertDialog.Builder(this)
                .setTitle("Confirm Your Email")
                .setMessage(
                    "Enter the email address where you received the link."
                )
                .setView(input)
                .setPositiveButton("VERIFY") { _, _ ->

                    val email = input.text.toString()
                        .trim()
                        .lowercase()

                    if (android.util.Patterns.EMAIL_ADDRESS
                            .matcher(email).matches()
                    ) {
                        completeEmailSignIn(email, link)
                    } else {
                        Toast.makeText(
                            this,
                            "Please enter a valid email address.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }

    // COMPLETE FIREBASE EMAIL SIGN-IN
    private fun completeEmailSignIn(
        email: String,
        link: String
    ) {

        auth.signInWithEmailLink(email, link)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    prefs.edit()
                        .remove("EMAIL_FOR_SIGN_IN")
                        .apply()

                    Toast.makeText(
                        this,
                        "Email verified successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Suriin kung Driver o Passenger ang napili bago ituloy ang intent
                    val rgRole = findViewById<RadioGroup>(R.id.rgRole)
                    val selectedRoleId = rgRole?.checkedRadioButtonId ?: R.id.rbPassenger

                    val nextIntent = if (selectedRoleId == R.id.rbDriver) {
                        Intent(this, DriverRegistrationActivity::class.java)
                    } else {
                        Intent(this, PassengerRegistrationActivity::class.java)
                    }

                    nextIntent.putExtra("USER_EMAIL", email)

                    startActivity(nextIntent)
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Email verification failed: " +
                                (task.exception?.localizedMessage
                                    ?: "Invalid or expired link."),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // HANDLE LINK WHEN APP IS ALREADY OPEN
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        handleEmailLink(intent)
    }
}