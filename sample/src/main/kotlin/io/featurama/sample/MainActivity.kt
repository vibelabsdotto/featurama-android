package io.featurama.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.featurama.sample.databinding.ActivityMainBinding
import io.featurama.sdk.Featurama
import io.featurama.sdk.exception.ConflictException
import io.featurama.sdk.exception.FeaturamaException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.submitButton.setOnClickListener {
            submitFeatureRequest()
        }

        binding.loadButton.setOnClickListener {
            loadFeatureRequests()
        }
    }

    private fun submitFeatureRequest() {
        val title = binding.titleInput.text?.toString()?.trim()
        val description = binding.descriptionInput.text?.toString()?.trim()?.ifEmpty { null }

        if (title.isNullOrBlank()) {
            binding.titleInputLayout.error = "Title is required"
            return
        }

        binding.titleInputLayout.error = null
        setLoading(true)

        lifecycleScope.launch {
            try {
                val request = Featurama.createFeatureRequest(
                    title = title,
                    description = description
                )

                binding.titleInput.text?.clear()
                binding.descriptionInput.text?.clear()

                showResult(buildString {
                    appendLine("Feature request created successfully!")
                    appendLine()
                    appendLine("ID: ${request.id}")
                    appendLine("Title: ${request.title}")
                    appendLine("Status: ${request.status}")
                    appendLine("Votes: ${request.voteCount}")
                })

                Toast.makeText(this@MainActivity, "Created!", Toast.LENGTH_SHORT).show()

            } catch (e: FeaturamaException) {
                showResult("Error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun loadFeatureRequests() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = Featurama.getFeatureRequests(page = 1, pageSize = 10)

                val result = buildString {
                    appendLine("Found ${response.totalCount} feature requests")
                    appendLine("Page ${response.page} of ${response.totalPages}")
                    appendLine()

                    if (response.isEmpty) {
                        appendLine("No feature requests yet.")
                    } else {
                        response.items.forEachIndexed { index, request ->
                            appendLine("${index + 1}. ${request.title}")
                            appendLine("   Status: ${request.status} | Votes: ${request.voteCount}")
                            if (request.description != null) {
                                appendLine("   ${request.description}")
                            }
                            appendLine()
                        }
                    }
                }

                showResult(result)

                // Example: Vote for the first request if available
                if (response.items.isNotEmpty()) {
                    val firstRequest = response.items.first()
                    try {
                        val updated = Featurama.vote(firstRequest.id)
                        Toast.makeText(
                            this@MainActivity,
                            "Voted for '${updated.title}'! (${updated.voteCount} votes)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: ConflictException) {
                        // Already voted - this is fine
                        Toast.makeText(
                            this@MainActivity,
                            "Already voted for '${firstRequest.title}'",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: FeaturamaException) {
                showResult("Error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.submitButton.isEnabled = !loading
        binding.loadButton.isEnabled = !loading
    }

    private fun showResult(text: String) {
        binding.resultText.text = text
    }
}
