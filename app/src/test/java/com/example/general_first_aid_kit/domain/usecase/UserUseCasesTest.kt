package com.example.general_first_aid_kit.domain.usecase

import android.net.Uri
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUserUseCaseTest {

    private val repository = mockk<UserRepository>()
    private lateinit var useCase: GetUserUseCase

    @Before
    fun setUp() {
        useCase = GetUserUseCase(repository)
    }

    @Test
    fun `should_returnUser_when_userExists`() {
        every { repository.getCurrentUser() } returns fakeUser()

        val result = useCase()

        assertEquals("user-1", result?.id)
    }

    @Test
    fun `should_returnNull_when_userNotFound`() {
        every { repository.getCurrentUser() } returns null

        val result = useCase()

        assertNull(result)
    }

    @Test
    fun `should_callGetCurrentUserOnRepository`() {
        every { repository.getCurrentUser() } returns null

        useCase()

        verify(exactly = 1) { repository.getCurrentUser() }
    }

    private fun fakeUser() = User(id = "user-1", email = "test@test.com", name = "Тест", avatarURL = null)
}

class GetUsersByIdsUseCaseTest {

    private val repository = mockk<UserRepository>()
    private lateinit var useCase: GetUsersByIdsUseCase

    @Before
    fun setUp() {
        useCase = GetUsersByIdsUseCase(repository)
    }

    @Test
    fun `should_returnUsersFromRepository`() = runTest {
        val users = listOf(fakeUser("u-1"), fakeUser("u-2"))
        coEvery { repository.getUsersByIds(any()) } returns users

        val result = useCase(listOf("u-1", "u-2"))

        assertEquals(2, result.size)
    }

    @Test
    fun `should_returnEmptyList_when_idsEmpty`() = runTest {
        coEvery { repository.getUsersByIds(emptyList()) } returns emptyList()

        val result = useCase(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should_passUserIdsToRepository`() = runTest {
        val ids = listOf("u-1", "u-2", "u-3")
        coEvery { repository.getUsersByIds(ids) } returns emptyList()

        useCase(ids)

        coVerify(exactly = 1) { repository.getUsersByIds(ids) }
    }

    private fun fakeUser(id: String) = User(id = id, email = "test@test.com", name = "Тест", avatarURL = null)
}

class UpdateUserUseCaseTest {

    private val repository = mockk<UserRepository>()
    private lateinit var useCase: UpdateUserUseCase

    @Before
    fun setUp() {
        useCase = UpdateUserUseCase(repository)
        coEvery { repository.updateUserProfile(any(), any()) } returns Result.success(Unit)
    }

    @Test
    fun `should_returnSuccess_when_updateSucceeds`() = runTest {
        val result = useCase("Иван", null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_updateFails`() = runTest {
        coEvery { repository.updateUserProfile(any(), any()) } returns
            Result.failure(Exception("Ошибка обновления"))

        val result = useCase("Иван", null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_passNameToRepository`() = runTest {
        useCase("Мария", null)

        coVerify(exactly = 1) { repository.updateUserProfile("Мария", null) }
    }

    @Test
    fun `should_passPhotoUriToRepository`() = runTest {
        val uri = mockk<Uri>()

        useCase("Иван", uri)

        coVerify(exactly = 1) { repository.updateUserProfile("Иван", uri) }
    }
}
