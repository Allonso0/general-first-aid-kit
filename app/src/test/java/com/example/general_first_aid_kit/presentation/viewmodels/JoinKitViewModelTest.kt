package com.example.general_first_aid_kit.presentation.viewmodels

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.JoinKitByCodeUseCase
import com.example.general_first_aid_kit.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class JoinKitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val joinKitByCodeUseCase = mockk<JoinKitByCodeUseCase>()
    private val getUserUseCase = mockk<GetUserUseCase>()
    private lateinit var viewModel: JoinKitViewModel

    @Before
    fun setUp() {
        viewModel = JoinKitViewModel(joinKitByCodeUseCase, getUserUseCase)
    }

    @Test
    fun `should_setErrorUserNotAuth_when_userIsNull`() = runTest {
        every { getUserUseCase() } returns null

        viewModel.joinKit("CODE1234") {}

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Пользователь не авторизован", viewModel.uiState.value.error)
    }

    @Test
    fun `should_notCallJoinUseCase_when_userIsNull`() = runTest {
        every { getUserUseCase() } returns null

        viewModel.joinKit("CODE1234") {}

        coVerify(exactly = 0) { joinKitByCodeUseCase(any(), any(), any()) }
    }

    @Test
    fun `should_clearLoading_when_userIsNull`() = runTest {
        every { getUserUseCase() } returns null

        viewModel.joinKit("CODE1234") {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_callOnSuccess_when_joinSucceeds`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns Result.success(fakeKit())
        var successCalled = false

        viewModel.joinKit("CODE1234") { successCalled = true }

        assertTrue(successCalled)
    }

    @Test
    fun `should_clearLoadingAfterSuccess`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns Result.success(fakeKit())

        viewModel.joinKit("CODE1234") {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_clearError_when_joinSucceeds`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns Result.success(fakeKit())

        viewModel.joinKit("CODE1234") {}

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should_passUserIdAndCodeToUseCase`() = runTest {
        every { getUserUseCase() } returns fakeUser(id = "user-99")
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns Result.success(fakeKit())

        viewModel.joinKit("MYCODE1") {}

        coVerify(exactly = 1) { joinKitByCodeUseCase("user-99", "MYCODE1", any()) }
    }

    @Test
    fun `should_passUserNameToUseCase`() = runTest {
        every { getUserUseCase() } returns fakeUser(name = "Алиса")
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns Result.success(fakeKit())

        viewModel.joinKit("CODE1234") {}

        coVerify { joinKitByCodeUseCase(any(), any(), "Алиса") }
    }

    @Test
    fun `should_setError_when_joinFails`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns
            Result.failure(Exception("Неверный код"))

        viewModel.joinKit("BADCODE") {}

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Неверный код", viewModel.uiState.value.error)
    }

    @Test
    fun `should_clearLoadingAfterFailure`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        viewModel.joinKit("BADCODE") {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_notCallOnSuccess_when_joinFails`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))
        var successCalled = false

        viewModel.joinKit("BADCODE") { successCalled = true }

        assertTrue(!successCalled)
    }

    @Test
    fun `should_setLoadingTrue_while_joinKitInProgress`() = runTest {
        every { getUserUseCase() } returns fakeUser()
        val deferred = CompletableDeferred<Result<Kit>>()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } coAnswers { deferred.await() }

        viewModel.joinKit("CODE1234") {}
        assertTrue(viewModel.uiState.value.isLoading)

        deferred.complete(Result.success(fakeKit()))
        advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    private fun fakeUser(id: String = "user-1", name: String = "Тест") = User(
        id = id,
        email = "test@test.com",
        name = name,
        avatarURL = null
    )

    private fun fakeKit() = Kit(
        id = "kit-1",
        name = "Аптечка",
        type = KitType.SHARED,
        userIds = listOf("user-1")
    )
}
