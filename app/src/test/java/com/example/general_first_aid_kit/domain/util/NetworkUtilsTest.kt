package com.example.general_first_aid_kit.domain.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NetworkUtilsTest {

    private val context = mockk<Context>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private val network = mockk<Network>()
    private val capabilities = mockk<NetworkCapabilities>()

    @Before
    fun setUp() {
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    }

    @Test
    fun `isInternetAvailable_returnsFalse_when_activeNetworkIsNull`() {
        every { connectivityManager.activeNetwork } returns null

        val result = NetworkUtils.isInternetAvailable(context)

        assertFalse(result)
    }

    @Test
    fun `isInternetAvailable_returnsFalse_when_capabilitiesAreNull`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        val result = NetworkUtils.isInternetAvailable(context)

        assertFalse(result)
    }

    @Test
    fun `isInternetAvailable_returnsTrue_when_internetCapabilityPresent`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        val result = NetworkUtils.isInternetAvailable(context)

        assertTrue(result)
    }

    @Test
    fun `isInternetAvailable_returnsFalse_when_internetCapabilityAbsent`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        val result = NetworkUtils.isInternetAvailable(context)

        assertFalse(result)
    }
}
