package com.example.general_first_aid_kit.presentation.viewmodels

import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.usecase.CreateKitUseCase
import com.example.general_first_aid_kit.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateKitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createKitUseCase = mockk<CreateKitUseCase>()
    private lateinit var viewModel: CreateKitViewModel

    @Before
    fun setUp() {
        viewModel = CreateKitViewModel(createKitUseCase)
    }

    @Test
    fun `should_updateName_when_onNameChange`() {
        viewModel.onNameChange("Домашняя аптечка")

        assertEquals("Домашняя аптечка", viewModel.uiState.value.name)
    }

    @Test
    fun `should_updateLocation_when_onLocationChange`() {
        viewModel.onLocationChange("Ванная комната")

        assertEquals("Ванная комната", viewModel.uiState.value.location)
    }

    @Test
    fun `should_updateColorIndex_when_onColorSelected`() {
        viewModel.onColorSelected(3)

        assertEquals(3, viewModel.uiState.value.colorIndex)
    }

    @Test
    fun `should_setIsSharedTrue_when_onTypeChangeTrue`() {
        viewModel.onTypeChange(isShared = true)

        assertTrue(viewModel.uiState.value.isShared)
    }

    @Test
    fun `should_setIsSharedFalse_when_onTypeChangeFalse`() {
        viewModel.onTypeChange(isShared = true)
        viewModel.onTypeChange(isShared = false)

        assertTrue(!viewModel.uiState.value.isShared)
    }

    @Test
    fun `should_callOnSuccess_when_createKitSucceeds`() = runTest {
        coEvery { createKitUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        var successCalled = false

        viewModel.onNameChange("Аптечка")
        viewModel.createKit { successCalled = true }

        assertTrue(successCalled)
    }

    @Test
    fun `should_clearLoadingAfterSuccess`() = runTest {
        coEvery { createKitUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.createKit {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_passNameAndTypeToUseCase`() = runTest {
        coEvery { createKitUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        viewModel.onNameChange("Семейная")
        viewModel.onTypeChange(isShared = true)

        viewModel.createKit {}

        coVerify {
            createKitUseCase(
                name = "Семейная",
                location = any(),
                colorIndex = any(),
                type = KitType.SHARED
            )
        }
    }

    @Test
    fun `should_setLoadingTrue_while_createKitInProgress`() = runTest {
        val deferred = CompletableDeferred<Result<Unit>>()
        coEvery { createKitUseCase(any(), any(), any(), any()) } coAnswers { deferred.await() }

        viewModel.createKit {}
        assertTrue(viewModel.uiState.value.isLoading)

        deferred.complete(Result.success(Unit))
        advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_setError_when_createKitFails`() = runTest {
        coEvery { createKitUseCase(any(), any(), any(), any()) } returns
            Result.failure(Exception("Аптечка с таким именем уже существует"))

        viewModel.createKit {}

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Аптечка с таким именем уже существует", viewModel.uiState.value.error)
    }

    @Test
    fun `should_clearLoadingAfterFailure`() = runTest {
        coEvery { createKitUseCase(any(), any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        viewModel.createKit {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_notCallOnSuccess_when_createKitFails`() = runTest {
        coEvery { createKitUseCase(any(), any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))
        var successCalled = false

        viewModel.createKit { successCalled = true }

        assertTrue(!successCalled)
    }
}
