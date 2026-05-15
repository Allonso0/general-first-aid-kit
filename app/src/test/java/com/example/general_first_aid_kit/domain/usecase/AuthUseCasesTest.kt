package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckAuthUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private lateinit var useCase: CheckAuthUseCase

    @Before
    fun setUp() {
        useCase = CheckAuthUseCase(repository)
    }

    @Test
    fun `should_returnTrue_when_userAuthenticated`() {
        every { repository.isUserAuthenticated() } returns true

        val result = useCase()

        assertTrue(result)
    }

    @Test
    fun `should_returnFalse_when_userNotAuthenticated`() {
        every { repository.isUserAuthenticated() } returns false

        val result = useCase()

        assertFalse(result)
    }
}

class SignInUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private lateinit var useCase: SignInUseCase

    @Before
    fun setUp() {
        useCase = SignInUseCase(repository)
    }

    @Test
    fun `should_returnSuccess_when_signInSucceeds`() = runTest {
        coEvery { repository.signIn(any(), any()) } returns Result.success(Unit)

        val result = useCase("user@test.com", "password123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_signInFails`() = runTest {
        coEvery { repository.signIn(any(), any()) } returns Result.failure(Exception("Неверный пароль"))

        val result = useCase("user@test.com", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Неверный пароль", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_passEmailAndPasswordToRepository`() = runTest {
        coEvery { repository.signIn("a@b.com", "pass") } returns Result.success(Unit)

        useCase("a@b.com", "pass")

        coVerify(exactly = 1) { repository.signIn("a@b.com", "pass") }
    }
}

class SignUpUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private lateinit var useCase: SignUpUseCase

    @Before
    fun setUp() {
        useCase = SignUpUseCase(repository)
    }

    @Test
    fun `should_returnSuccess_when_signUpSucceeds`() = runTest {
        coEvery { repository.signUp(any(), any(), any()) } returns Result.success(Unit)

        val result = useCase("user@test.com", "password123", "Иван")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_signUpFails`() = runTest {
        coEvery { repository.signUp(any(), any(), any()) } returns
            Result.failure(Exception("Email уже используется"))

        val result = useCase("user@test.com", "pass", "Иван")

        assertTrue(result.isFailure)
        assertEquals("Email уже используется", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_passAllParamsToRepository`() = runTest {
        coEvery { repository.signUp("e@e.com", "p123", "Пётр") } returns Result.success(Unit)

        useCase("e@e.com", "p123", "Пётр")

        coVerify(exactly = 1) { repository.signUp("e@e.com", "p123", "Пётр") }
    }
}

class SignOutUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private lateinit var useCase: SignOutUseCase

    @Before
    fun setUp() {
        useCase = SignOutUseCase(repository)
        justRun { repository.signOut() }
    }

    @Test
    fun `should_callRepositorySignOut`() {
        useCase()

        verify(exactly = 1) { repository.signOut() }
    }
}
