package com.tubeboost.ai.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.tubeboost.ai.databinding.ActivityMainBinding
import com.tubeboost.ai.model.GenerationState
import com.tubeboost.ai.model.SeoResult
import com.tubeboost.ai.utils.AdManager
import com.tubeboost.ai.viewmodel.MainViewModel

/**
 * MainActivity - Main home screen of TubeBoost AI
 * Handles user input and coordinates with ViewModel for content generation
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // Track which generation type was requested
    private var pendingGenerationType = GenerationType.FULL

    enum class GenerationType {
        FULL, TAGS_ONLY, TITLES_ONLY, DESCRIPTION_ONLY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupAds()
    }

    private fun setupUI() {
        // Input field keyboard action
        binding.etVideoTopic.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                triggerGeneration(GenerationType.FULL)
                true
            } else {
                false
            }
        }

        // Clear error when user types
        binding.etVideoTopic.addTextChangedListener {
            binding.tilVideoTopic.error = null
        }

        // ── Button click listeners ─────────────────────────────────────────────
        binding.btnGenerateTags.setOnClickListener {
            pendingGenerationType = GenerationType.FULL
            triggerGeneration(GenerationType.FULL)
        }

        binding.btnGenerateTitles.setOnClickListener {
            pendingGenerationType = GenerationType.TITLES_ONLY
            triggerGeneration(GenerationType.FULL) // API generates all, we show titles
        }

        binding.btnGenerateDescription.setOnClickListener {
            pendingGenerationType = GenerationType.DESCRIPTION_ONLY
            triggerGeneration(GenerationType.FULL) // API generates all, we show description
        }

        // ── Feature cards ─────────────────────────────────────────────────────
        binding.cardSeoTags.setOnClickListener {
            setTopicHint("SEO tags for my video")
            binding.etVideoTopic.requestFocus()
        }

        binding.cardHashtags.setOnClickListener {
            setTopicHint("trending hashtags for")
            binding.etVideoTopic.requestFocus()
        }

        binding.cardViralTitles.setOnClickListener {
            setTopicHint("viral titles for")
            binding.etVideoTopic.requestFocus()
        }

        binding.cardTrendingIdeas.setOnClickListener {
            setTopicHint("trending video ideas about")
            binding.etVideoTopic.requestFocus()
        }

        // Privacy policy link
        binding.tvPrivacyPolicy.setOnClickListener {
            // TODO: Replace with your actual privacy policy URL
            showSnackbar("Privacy Policy: https://yourwebsite.com/privacy")
        }
    }

    private fun setTopicHint(hint: String) {
        if (binding.etVideoTopic.text.isNullOrEmpty()) {
            binding.etVideoTopic.hint = hint
        }
    }

    private fun setupObservers() {
        viewModel.generationState.observe(this) { state ->
            when (state) {
                is GenerationState.Idle -> {
                    showLoadingState(false)
                }

                is GenerationState.Loading -> {
                    showLoadingState(true)
                }

                is GenerationState.Success -> {
                    showLoadingState(false)
                    handleSuccess(state.result)
                }

                is GenerationState.Error -> {
                    showLoadingState(false)
                    handleError(state.message)
                }
            }
        }
    }

    private fun handleSuccess(result: SeoResult) {
        // Show interstitial based on frequency (every 3 generations)
        AdManager.showInterstitialIfReady(this)

        // Navigate to ResultActivity with the result
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_SEO_RESULT, result)
            putExtra(ResultActivity.EXTRA_GENERATION_TYPE, pendingGenerationType.name)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun handleError(message: String) {
        // Validate input error → show in TextInputLayout
        if (message.contains("enter", ignoreCase = true) || 
            message.contains("too short", ignoreCase = true) ||
            message.contains("too long", ignoreCase = true)) {
            binding.tilVideoTopic.error = message
        } else {
            // Network/API error → show snackbar
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction("Retry") {
                    triggerGeneration(pendingGenerationType)
                }
                .show()
        }
    }

    private fun triggerGeneration(type: GenerationType) {
        hideKeyboard()
        val topic = binding.etVideoTopic.text?.toString() ?: ""
        pendingGenerationType = type
        viewModel.generateContent(this, topic)
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
        binding.btnGenerateTags.isEnabled = !isLoading
        binding.btnGenerateTitles.isEnabled = !isLoading
        binding.btnGenerateDescription.isEnabled = !isLoading
        binding.etVideoTopic.isEnabled = !isLoading

        if (isLoading) {
            binding.btnGenerateTags.alpha = 0.6f
            binding.btnGenerateTitles.alpha = 0.6f
            binding.btnGenerateDescription.alpha = 0.6f
            binding.tvLoadingHint.visibility = View.VISIBLE
        } else {
            binding.btnGenerateTags.alpha = 1f
            binding.btnGenerateTitles.alpha = 1f
            binding.btnGenerateDescription.alpha = 1f
            binding.tvLoadingHint.visibility = View.GONE
        }
    }

    private fun setupAds() {
        try {
            AdManager.loadBannerAd(this, binding.adBannerContainer)
        } catch (e: Exception) {
            binding.adBannerContainer.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        } catch (e: Exception) {
            // Ignore keyboard hiding errors
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Reset state when returning from result screen
        viewModel.resetState()
    }
}
