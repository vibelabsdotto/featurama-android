package io.featurama.sdk

import org.junit.Assert.*
import org.junit.Test

class FeaturamaConfigTest {

    @Test
    fun `build with valid API key succeeds`() {
        val config = FeaturamaConfig.Builder("fm_live_test123")
            .build()

        assertEquals("fm_live_test123", config.apiKey)
        assertEquals(FeaturamaConfig.DEFAULT_BASE_URL, config.baseUrl)
        assertNull(config.defaultUserIdentifier)
    }

    @Test
    fun `build with all options succeeds`() {
        val config = FeaturamaConfig.Builder("fm_live_test123")
            .baseUrl("https://custom.api.com")
            .defaultUserIdentifier("user_456")
            .connectTimeout(10_000)
            .readTimeout(20_000)
            .writeTimeout(15_000)
            .build()

        assertEquals("fm_live_test123", config.apiKey)
        assertEquals("https://custom.api.com", config.baseUrl)
        assertEquals("user_456", config.defaultUserIdentifier)
        assertEquals(10_000L, config.connectTimeoutMs)
        assertEquals(20_000L, config.readTimeoutMs)
        assertEquals(15_000L, config.writeTimeoutMs)
    }

    @Test
    fun `baseUrl removes trailing slash`() {
        val config = FeaturamaConfig.Builder("fm_live_test123")
            .baseUrl("https://custom.api.com/")
            .build()

        assertEquals("https://custom.api.com", config.baseUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `build with blank API key throws`() {
        FeaturamaConfig.Builder("   ")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `build with empty API key throws`() {
        FeaturamaConfig.Builder("")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `connectTimeout with zero throws`() {
        FeaturamaConfig.Builder("fm_live_test123")
            .connectTimeout(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `readTimeout with negative value throws`() {
        FeaturamaConfig.Builder("fm_live_test123")
            .readTimeout(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `writeTimeout with negative value throws`() {
        FeaturamaConfig.Builder("fm_live_test123")
            .writeTimeout(-100)
    }
}
