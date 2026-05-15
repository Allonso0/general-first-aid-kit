package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.example.general_first_aid_kit.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateKitUseCaseTest {

    private val kitRepository = mockk<KitRepository>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var useCase: CreateKitUseCase

    @Before
    fun setUp() {
        useCase = CreateKitUseCase(kitRepository, userRepository)
    }

    @Test
    fun `should_returnFailure_when_nameIsBlank`() = runTest {
        val result = useCase(name = "", location = "Дом", colorIndex = 0, type = KitType.PERSONAL)

        assertTrue(result.isFailure)
        assertEquals("Название аптечки не может быть пустым", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_returnFailure_when_nameIsWhitespaceOnly`() = runTest {
        val result = useCase(name = "   ", location = "", colorIndex = 0, type = KitType.PERSONAL)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_notCallRepository_when_nameIsBlank`() = runTest {
        useCase(name = "", location = "", colorIndex = 0, type = KitType.PERSONAL)

        coVerify(exactly = 0) { kitRepository.createKit(any()) }
    }

    @Test
    fun `should_returnFailure_when_userNotFound`() = runTest {
        every { userRepository.getCurrentUser() } returns null

        val result = useCase(name = "Аптечка", location = "", colorIndex = 0, type = KitType.PERSONAL)

        assertTrue(result.isFailure)
        assertEquals("Пользователь не найден", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_notCallRepository_when_userNotFound`() = runTest {
        every { userRepository.getCurrentUser() } returns null

        useCase(name = "Аптечка", location = "", colorIndex = 0, type = KitType.PERSONAL)

        coVerify(exactly = 0) { kitRepository.createKit(any()) }
    }

    @Test
    fun `should_returnSuccess_when_personalKitCreated`() = runTest {
        every { userRepository.getCurrentUser() } returns fakeUser()
        coEvery { kitRepository.createKit(any()) } returns Result.success(Unit)

        val result = useCase(name = "Домашняя", location = "Ванная", colorIndex = 2, type = KitType.PERSONAL)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_notSetInviteCode_when_typeIsPersonal`() = runTest {
        val kitSlot = slot<Kit>()
        every { userRepository.getCurrentUser() } returns fakeUser()
        coEvery { kitRepository.createKit(capture(kitSlot)) } returns Result.success(Unit)

        useCase(name = "Личная", location = "", colorIndex = 0, type = KitType.PERSONAL)

        assertNull(kitSlot.captured.inviteCode)
    }

    @Test
    fun `should_setInviteCode_when_typeIsShared`() = runTest {
        val kitSlot = slot<Kit>()
        every { userRepository.getCurrentUser() } returns fakeUser()
        coEvery { kitRepository.createKit(capture(kitSlot)) } returns Result.success(Unit)

        useCase(name = "Общая", location = "", colorIndex = 0, type = KitType.SHARED)

        assertNotNull(kitSlot.captured.inviteCode)
        assertEquals(8, kitSlot.captured.inviteCode!!.length)
    }

    @Test
    fun `should_setInviteCodeToUppercase_when_typeIsShared`() = runTest {
        val kitSlot = slot<Kit>()
        every { userRepository.getCurrentUser() } returns fakeUser()
        coEvery { kitRepository.createKit(capture(kitSlot)) } returns Result.success(Unit)

        useCase(name = "Общая", location = "", colorIndex = 0, type = KitType.SHARED)

        val code = kitSlot.captured.inviteCode!!
        assertEquals(code, code.uppercase())
    }

    @Test
    fun `should_setOwnerIdFromCurrentUser`() = runTest {
        val kitSlot = slot<Kit>()
        every { userRepository.getCurrentUser() } returns fakeUser(id = "user-42")
        coEvery { kitRepository.createKit(capture(kitSlot)) } returns Result.success(Unit)

        useCase(name = "Аптечка", location = "", colorIndex = 0, type = KitType.PERSONAL)

        assertEquals("user-42", kitSlot.captured.ownerId)
    }

    @Test
    fun `should_setCurrentUserAsOnlyMember_when_kitCreated`() = runTest {
        val kitSlot = slot<Kit>()
        every { userRepository.getCurrentUser() } returns fakeUser(id = "user-42")
        coEvery { kitRepository.createKit(capture(kitSlot)) } returns Result.success(Unit)

        useCase(name = "Аптечка", location = "", colorIndex = 0, type = KitType.PERSONAL)

        assertEquals(listOf("user-42"), kitSlot.captured.userIds)
    }

    @Test
    fun `should_propagateRepositoryFailure`() = runTest {
        every { userRepository.getCurrentUser() } returns fakeUser()
        coEvery { kitRepository.createKit(any()) } returns Result.failure(Exception("Ошибка сети"))

        val result = useCase(name = "Аптечка", location = "", colorIndex = 0, type = KitType.PERSONAL)

        assertTrue(result.isFailure)
        assertEquals("Ошибка сети", result.exceptionOrNull()?.message)
    }

    private fun fakeUser(id: String = "user-1") = User(
        id = id,
        email = "test@test.com",
        name = "Тест",
        avatarURL = null
    )
}
