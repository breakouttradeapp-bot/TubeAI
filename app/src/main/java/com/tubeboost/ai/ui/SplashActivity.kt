package com.tubeboost.ai.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.tubeboost.ai.databinding.ActivitySplashBinding

/**
 * SplashActivity - Animated launch screen
 * Shows for 2.5 seconds with logo animation, then navigates to MainActivity
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DURATION = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide system bars for full immersive splash
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        playAnimations()
        scheduleNavigation()
    }

    private fun playAnimations() {
        // ── Logo icon scale + fade in ──────────────────────────────────────────
        val logoScaleAnim = ScaleAnimation(
            0.4f, 1f,
            0.4f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply { duration = 700 }

        val logoFadeAnim = AlphaAnimation(0f, 1f).apply { duration = 700 }

        val logoSet = AnimationSet(true).apply {
            addAnimation(logoScaleAnim)
            addAnimation(logoFadeAnim)
            fillAfter = true
        }

        binding.ivLogo.startAnimation(logoSet)

        // ── App name slide up + fade in ────────────────────────────────────────
        val titleSlideAnim = TranslateAnimation(
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_SELF, 0.3f,
            Animation.ABSOLUTE, 0f
        ).apply { duration = 600; startOffset = 400 }

        val titleFadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = 600
            startOffset = 400
        }

        val titleSet = AnimationSet(true).apply {
            addAnimation(titleSlideAnim)
            addAnimation(titleFadeAnim)
            fillAfter = true
        }

        binding.tvAppName.startAnimation(titleSet)

        // ── Tagline fade in ────────────────────────────────────────────────────
        val taglineFade = AlphaAnimation(0f, 1f).apply {
            duration = 600
            startOffset = 800
            fillAfter = true
        }
        binding.tvTagline.startAnimation(taglineFade)

        // ── Loading bar fade in ────────────────────────────────────────────────
        val progressFade = AlphaAnimation(0f, 1f).apply {
            duration = 400
            startOffset = 1200
            fillAfter = true
        }
        binding.progressBar.startAnimation(progressFade)
    }

    private fun scheduleNavigation() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                navigateToMain()
            }
        }, SPLASH_DURATION)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Smooth cross-fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    // Prevent back press during splash
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing
    }
}
