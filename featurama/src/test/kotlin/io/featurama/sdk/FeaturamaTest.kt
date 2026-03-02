package io.featurama.sdk

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FeaturamaTest {

    @Before
    fun setup() {
        Featurama.reset()
    }

    @After
    fun tearDown() {
        Featurama.reset()
    }

    @Test
    fun `init with API key succeeds`() {
        Featurama.init("fm_live_test123")

        assertTrue(Featurama.isInitialized())
        assertEquals("fm_live_test123", Featurama.getConfig().apiKey)
    }

    @Test
    fun `init with configuration block succeeds`() {
        Featurama.init("fm_live_test123") {
            baseUrl("https://custom.api.com")
            defaultUserIdentifier("user_456")
        }

        assertTrue(Featurama.isInitialized())
        assertEquals("https://custom.api.com", Featurama.getConfig().baseUrl)
        assertEquals("user_456", Featurama.getUserIdentifier())
    }

    @Test
    fun `init with config object succeeds`() {
        val config = FeaturamaConfig.Builder("fm_live_test123")
            .baseUrl("https://custom.api.com")
            .build()

        Featurama.init(config)

        assertTrue(Featurama.isInitialized())
        assertEquals("https://custom.api.com", Featurama.getConfig().baseUrl)
    }

    @Test
    fun `setUserIdentifier updates identifier`() {
        Featurama.init("fm_live_test123")

        Featurama.setUserIdentifier("new_user")

        assertEquals("new_user", Featurama.getUserIdentifier())
    }

    @Test
    fun `setUserIdentifier with null clears identifier`() {
        Featurama.init("fm_live_test123") {
            defaultUserIdentifier("initial_user")
        }

        Featurama.setUserIdentifier(null)

        assertNull(Featurama.getUserIdentifier())
    }

    @Test
    fun `isInitialized returns false before init`() {
        assertFalse(Featurama.isInitialized())
    }

    @Test(expected = IllegalStateException::class)
    fun `getClient before init throws`() {
        Featurama.getClient()
    }

    @Test(expected = IllegalStateException::class)
    fun `getConfig before init throws`() {
        Featurama.getConfig()
    }

    @Test(expected = IllegalStateException::class)
    fun `setUserIdentifier before init throws`() {
        Featurama.setUserIdentifier("user")
    }

    @Test
    fun `reinitializing SDK updates configuration`() {
        Featurama.init("fm_live_first")
        assertEquals("fm_live_first", Featurama.getConfig().apiKey)

        Featurama.init("fm_live_second")
        assertEquals("fm_live_second", Featurama.getConfig().apiKey)
    }
}
