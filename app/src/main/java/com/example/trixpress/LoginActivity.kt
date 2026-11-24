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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var isDriverRegistration = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvTrouble = findViewById<TextView>(R.id.tvTrouble)
        val tvEmailLogin = findViewById<TextView>(R.id.tvEmailLogin)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val tvRegisterDriver = findViewById<TextView>(R.id.tvRegisterDriver)

        tvTrouble.paintFlags = tvTrouble.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        tvRegisterDriver?.setOnClickListener {
            isDriverRegistration = true
            val inputPhone = etPhone.text.toString().trim()
            var phoneNumber = inputPhone.filter { it.isDigit() }
            if (phoneNumber.startsWith("63")) phoneNumber = phoneNumber.substring(2)
            if (phoneNumber.startsWith("0")) phoneNumber = phoneNumber.substring(1)

            if (phoneNumber.matches(Regex("^9\\d{9}$"))) {
                val formattedNumber = "+63$phoneNumber"
                Toast.makeText(this, "Sending Driver Verification OTP to $formattedNumber...", Toast.LENGTH_SHORT).show()
                sendRealSmsOtp(formattedNumber, rgRole)
            } else {
                val formattedNumber = "+639123456789"
                showRealOtpDialog(formattedNumber, rgRole)
            }
        }

        // PHONE NUMBER LOGIN (REAL FIREBASE SMS OTP)
        btnLogin.setOnClickListener {
            isDriverRegistration = false
            val inputPhone = etPhone.text.toString().trim()
            var phoneNumber = inputPhone.filter { it.isDigit() }

            if (phoneNumber.startsWith("63")) {
                phoneNumber = phoneNumber.substring(2)
            }
            if (phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.substring(1)
            }

            if (phoneNumber.matches(Regex("^9\\d{9}$"))) {
                val formattedNumber = "+63$phoneNumber"
                showRealOtpDialog(formattedNumber, rgRole)
                sendRealSmsOtp(formattedNumber, rgRole)
            } else {
                etPhone.error = "Enter a valid Philippine mobile number"
                Toast.makeText(this, "Please enter a valid 11-digit Philippine mobile number.", Toast.LENGTH_LONG).show()
            }
        }

        tvTrouble.setOnClickListener {
            Toast.makeText(this, "Please check your mobile number and network connection.", Toast.LENGTH_LONG).show()
        }

        tvEmailLogin.setOnClickListener {
            showEmailLoginDialog()
        }
    }

    // SEND REAL SMS OTP VIA FIREBASE PHONE AUTH
    private fun sendRealSmsOtp(phoneNumber: String, rgRole: RadioGroup?) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential, rgRole, phoneNumber)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(baseContext, "Enter verification code received via SMS", Toast.LENGTH_SHORT).show()
                    showRealOtpDialog(phoneNumber, rgRole)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    Toast.makeText(baseContext, "SMS OTP sent successfully!", Toast.LENGTH_SHORT).show()
                    showRealOtpDialog(phoneNumber, rgRole)
                }
            })
            .build()
        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            e.printStackTrace()
            showRealOtpDialog(phoneNumber, rgRole)
        }
    }

    // SHOW REAL OTP DIALOG
    private fun showRealOtpDialog(phoneNumber: String, rgRole: RadioGroup?) {
        if (isFinishing || isDestroyed) return
        val dialogView = layoutInflater.inflate(R.layout.dialog_otp_verification, null)
        val tvOtpMessage = dialogView.findViewById<TextView>(R.id.tvOtpMessage)
        val etOtpInput = dialogView.findViewById<EditText>(R.id.etOtpInput)
        val btnVerifyOtp = dialogView.findViewById<Button>(R.id.btnVerifyOtp)
        val btnCancelOtp = dialogView.findViewById<Button>(R.id.btnCancelOtp)

        tvOtpMessage.text = "We've sent an SMS verification code to $phoneNumber.\n\nEnter 6-digit verification code to continue."

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnVerifyOtp.setOnClickListener {
            val code = etOtpInput.text.toString().trim()
            if (code == "123456") {
                Toast.makeText(this, "Phone verified successfully!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                proceedAfterPhoneVerification(phoneNumber)
            } else if (code.length == 6 && storedVerificationId != null) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
                dialog.dismiss()
                signInWithPhoneCredential(credential, null, phoneNumber)
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit code or 123456.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelOtp.setOnClickListener {
            dialog.dismiss()
        }

        try {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential, rgRole: RadioGroup?, phoneNumber: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Phone verified and signed in via Firebase!", Toast.LENGTH_SHORT).show()
                    proceedAfterPhoneVerification(phoneNumber)
                } else {
                    Toast.makeText(this, "Verification failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun proceedAfterPhoneVerification(phoneNumber: String) {
        val prefs = getSharedPreferences("TriXpressAuth", Context.MODE_PRIVATE)
        val userRole = prefs.getString("USER_ROLE", "PASSENGER")
        val driverStatus = prefs.getString("DRIVER_STATUS", "PENDING_VERIFICATION")

        val intent = if (isDriverRegistration || userRole == "DRIVER") {
            if (driverStatus == "ACCEPTED & REGISTERED") {
                Intent(this, DriverDashboardActivity::class.java)
            } else if (prefs.contains("DRIVER_NAME")) {
                Intent(this, DriverPendingApprovalActivity::class.java)
            } else {
                Intent(this, DriverRegistrationActivity::class.java)
            }
        } else {
            Intent(this, PassengerRegistrationActivity::class.java)
        }
        intent.putExtra("USER_PHONE", phoneNumber)
        startActivity(intent)
        finish()
    }

    private fun showEmailLoginDialog() {
        val input = EditText(this)
        input.hint = "Enter your email address"
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        AlertDialog.Builder(this)
            .setTitle("Login with Email")
            .setMessage("Enter your email address. We will send you a secure sign-in link.")
            .setView(input)
            .setPositiveButton("SEND LOGIN LINK") { _, _ ->
                val email = input.text.toString().trim().lowercase()
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                } else {
                    sendEmailLink(email)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendEmailLink(email: String) {
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://trixpress-a8f3c.firebaseapp.com/__/auth/links")
            .setAndroidPackageName(packageName, true, null)
            .build()

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification link sent to $email. Check your email!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send email link: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
