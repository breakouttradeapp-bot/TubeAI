package com.tubeboost.ai.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.tubeboost.ai.R
import com.tubeboost.ai.databinding.ActivityResultBinding
import com.tubeboost.ai.model.GenerationState
import com.tubeboost.ai.model.SeoResult
import com.tubeboost.ai.utils.AdManager
import com.tubeboost.ai.viewmodel.MainViewModel

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val viewModel: MainViewModel by viewModels()
    private var currentResult: SeoResult? = null

    companion object {
        const val EXTRA_SEO_RESULT = "extra_seo_result"
        const val EXTRA_GENERATION_TYPE = "extra_generation_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentResult = intent.getParcelableExtra(EXTRA_SEO_RESULT)

        if (currentResult == null) {
            showErrorAndFinish()
            return
        }

        setupUI()
        displayResults(currentResult!!)
        setupAds()
        setupBonusObserver()
    }

    private fun setupUI() {

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Copy Tags
        binding.btnCopyTags.setOnClickListener {
            currentResult?.let {
                copyToClipboard("SEO Tags", it.getTagsFormatted())
                showSnackbar("SEO tags copied")
            }
        }

        // Share Result
        binding.btnShareResult.setOnClickListener {
            currentResult?.let {
                shareResult(it)
            }
        }

        // Generate Again
        binding.btnGenerateAgain.setOnClickListener {
            finish()
        }

        binding.btnCopyTitles.setOnClickListener {
            currentResult?.let {
                copyToClipboard("Titles", it.getTitlesFormatted())
            }
        }

        binding.btnCopyDescription.setOnClickListener {
            currentResult?.let {
                copyToClipboard("Description", it.description)
            }
        }

        binding.btnCopyHashtags.setOnClickListener {
            currentResult?.let {
                copyToClipboard("Hashtags", it.getHashtagsFormatted())
            }
        }

        binding.btnUnlockBonusTags.setOnClickListener {
            showBonusTagsDialog()
        }
    }

    private fun displayResults(result: SeoResult) {

        binding.tvTopicTitle.text = result.topic

        if (result.tags.isNotEmpty()) {
            binding.cardSeoTags.isVisible = true
            populateChipGroup(result.tags)
            binding.tvTagCount.text = "${result.tags.size} tags"
        } else {
            binding.cardSeoTags.isVisible = false
        }

        if (result.titles.isNotEmpty()) {
            binding.cardTitles.isVisible = true
            binding.tvTitlesContent.text = result.getTitlesFormatted()
        } else {
            binding.cardTitles.isVisible = false
        }

        if (result.description.isNotEmpty()) {
            binding.cardDescription.isVisible = true
            binding.tvDescriptionContent.text = result.description
        } else {
            binding.cardDescription.isVisible = false
        }

        if (result.hashtags.isNotEmpty()) {
            binding.cardHashtags.isVisible = true
            populateHashtagChips(result.hashtags)
            binding.tvHashtagCount.text = "${result.hashtags.size} hashtags"
        } else {
            binding.cardHashtags.isVisible = false
        }
    }

    private fun populateChipGroup(tags: List<String>) {
        binding.chipGroupTags.removeAllViews()

        tags.forEach { tag ->
            val chip = Chip(this).apply {
                text = tag
                isClickable = true
                isCheckable = false
                setOnClickListener {
                    copyToClipboard("Tag", tag)
                }
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    private fun populateHashtagChips(hashtags: List<String>) {
        binding.chipGroupHashtags.removeAllViews()

        hashtags.forEach { tag ->
            val chip = Chip(this).apply {
                text = tag
                isClickable = true
                isCheckable = false
                setOnClickListener {
                    copyToClipboard("Hashtag", tag)
                }
            }
            binding.chipGroupHashtags.addView(chip)
        }
    }

    private fun showBonusTagsDialog() {

        if (AdManager.isRewardedAdReady()) {

            MaterialAlertDialogBuilder(this)
                .setTitle("Unlock Bonus Tags")
                .setMessage("Watch an ad to unlock extra SEO tags.")
                .setPositiveButton("Watch") { _, _ ->
                    showRewardedAd()
                }
                .setNegativeButton("Cancel", null)
                .show()

        } else {
            showSnackbar("Ad loading, try again")
            AdManager.loadRewardedAd(this)
        }
    }

    private fun showRewardedAd() {

        AdManager.showRewardedAd(
            activity = this,
            onRewarded = { _, _ ->
                viewModel.generateBonusTags(this)
            },
            onNotAvailable = {
                viewModel.generateBonusTags(this)
            }
        )
    }

    private fun setupBonusObserver() {

        viewModel.bonusTagsState.observe(this) { state ->

            when (state) {

                is GenerationState.Loading -> {
                    binding.bonusLoadingBar.isVisible = true
                }

                is GenerationState.Success -> {

                    binding.bonusLoadingBar.isVisible = false

                    currentResult?.let { existing ->

                        val merged = existing.copy(
                            tags = (existing.tags + state.result.tags).distinct(),
                            hashtags = (existing.hashtags + state.result.hashtags).distinct()
                        )

                        currentResult = merged
                        displayResults(merged)

                        binding.cardBonusTags.isVisible = false
                    }
                }

                is GenerationState.Error -> {
                    binding.bonusLoadingBar.isVisible = false
                    showSnackbar(state.message)
                }

                else -> {}
            }
        }
    }

    private fun setupAds() {
        try {
            AdManager.loadBannerAd(this, binding.adBannerContainer)
        } catch (e: Exception) {
            binding.adBannerContainer.visibility = View.GONE
        }
    }

    private fun copyToClipboard(label: String, text: String) {

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareResult(result: SeoResult) {

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, result.getFullShareText())
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Share"))
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorAndFinish() {
        Toast.makeText(this, "Unable to load result", Toast.LENGTH_LONG).show()
        finish()
    }
}
