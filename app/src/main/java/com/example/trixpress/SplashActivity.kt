
package com.example.trixpress

import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.widget.ProgressBar
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Connect the loading bar
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Animate loading from 0% to 100%
        val animator = ValueAnimator.ofInt(0, 100)

        animator.duration = 3000 // 3 seconds

        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }

        // Open LoginActivity after loading
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {

                val intent = Intent(
                    this@SplashActivity,
                    LoginActivity::class.java
                )

                startActivity(intent)
                finish()
            }
        })

        // Start animation
        animator.start()
    }
}
