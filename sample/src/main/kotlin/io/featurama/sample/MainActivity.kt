package io.featurama.sample

import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CancellationException
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.featurama.sample.databinding.ActivityMainBinding
import io.featurama.sdk.Featurama
import io.featurama.sdk.exception.FeaturamaException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var emailCollection: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadConfig()
    }

    private fun loadConfig() {
        binding.submitButton.isEnabled = false
        lifecycleScope.launch {
            try {
                emailCollection = Featurama.getProjectConfig().emailCollection
                binding.emailInputLayout.visibility = if (emailCollection == "none") View.GONE else View.VISIBLE
                binding.emailInputLayout.hint = if (emailCollection == "required") "Email address, required" else "Email address, optional"
                binding.submitButton.isEnabled = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: FeaturamaException) {
                showResult("Configuration failed: ${e.message}. Use Load to retry.")
            }
        }
    }

    private fun setupListeners() {
        binding.boardButton.setOnClickListener {
            startActivity(Intent(this, BoardActivity::class.java))
        }
        binding.submitButton.setOnClickListener {
            submitFeatureRequest()
        }

        binding.loadButton.setOnClickListener {
            if (emailCollection == null) loadConfig()
            loadFeatureRequests()
        }
    }

    private fun submitFeatureRequest() {
        if (emailCollection == null) return
        val email = binding.emailInput.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        if (emailCollection != "none" && (email != null || emailCollection == "required") &&
            (email == null || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            binding.emailInputLayout.error = "Enter a valid email address"
            return
        }
        binding.emailInputLayout.error = null
        val title = binding.titleInput.text?.toString()?.trim()
        val description = binding.descriptionInput.text?.toString()?.trim()

        if (title.isNullOrBlank()) {
            binding.titleInputLayout.error = "Title is required"
            return
        }

        if (description.isNullOrBlank()) {
            binding.descriptionInputLayout.error = "Description is required"
            return
        }

        binding.titleInputLayout.error = null
        binding.descriptionInputLayout.error = null
        setLoading(true)

        lifecycleScope.launch {
            try {
                val request = Featurama.createFeatureRequest(
                    title = title,
                    description = description,
                    email = if (emailCollection == "none") null else email
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

            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                showResult("Error: ${e.message}")
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


            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                showResult("Error: ${e.message}")
            } catch (e: FeaturamaException) {
                showResult("Error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.submitButton.isEnabled = !loading && emailCollection != null
        binding.loadButton.isEnabled = !loading
    }

    private fun showResult(text: String) {
        binding.resultText.text = text
    }
}
