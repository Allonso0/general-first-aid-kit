package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.repository.AppSettingsRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAppSettingsUseCaseTest {

    private val repository = mockk<AppSettingsRepository>()
    private lateinit var useCase: GetAppSettingsUseCase

    @Before
    fun setUp() {
        useCase = GetAppSettingsUseCase(repository)
    }

    @Test
    fun `should_returnSettingsFromRepository`() {
        every { repository.getSettings() } returns AppSettings(lowStockThreshold = 3, expiryWarningDays = 14)

        val result = useCase()

        assertEquals(3, result.lowStockThreshold)
        assertEquals(14, result.expiryWarningDays)
    }

    @Test
    fun `should_callGetSettingsOnRepository`() {
        every { repository.getSettings() } returns AppSettings()

        useCase()

        verify(exactly = 1) { repository.getSettings() }
    }
}

class SaveAppSettingsUseCaseTest {

    private val repository = mockk<AppSettingsRepository>()
    private lateinit var useCase: SaveAppSettingsUseCase

    @Before
    fun setUp() {
        useCase = SaveAppSettingsUseCase(repository)
        justRun { repository.saveSettings(any()) }
    }

    @Test
    fun `should_callSaveSettingsWithGivenSettings`() {
        val settings = AppSettings(lowStockThreshold = 5, expiryWarningDays = 30)

        useCase(settings)

        verify(exactly = 1) { repository.saveSettings(settings) }
    }

    @Test
    fun `should_passCorrectSettings`() {
        val settings = AppSettings(lowStockThreshold = 1, expiryWarningDays = 7)

        useCase(settings)

        verify { repository.saveSettings(match { it.lowStockThreshold == 1 && it.expiryWarningDays == 7 }) }
    }
}
