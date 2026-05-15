package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.repository.AuthRepository
import com.example.general_first_aid_kit.domain.repository.KitRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetKitUseCaseTest {

    private val repository = mockk<KitRepository>()
    private lateinit var useCase: GetKitUseCase

    @Before
    fun setUp() {
        useCase = GetKitUseCase(repository)
    }

    @Test
    fun `should_returnSuccess_when_kitExists`() = runTest {
        coEvery { repository.getKitById("kit-1") } returns Result.success(fakeKit())

        val result = useCase("kit-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_kitNotFound`() = runTest {
        coEvery { repository.getKitById(any()) } returns Result.failure(Exception("not found"))

        val result = useCase("kit-1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_passKitIdToRepository`() = runTest {
        coEvery { repository.getKitById("kit-42") } returns Result.success(fakeKit())

        useCase("kit-42")

        coVerify(exactly = 1) { repository.getKitById("kit-42") }
    }

    private fun fakeKit() = Kit(id = "kit-1", name = "Аптечка", type = KitType.PERSONAL, userIds = listOf("u"))
}

class DeleteKitUseCaseTest {

    private val repository = mockk<KitRepository>()
    private lateinit var useCase: DeleteKitUseCase

    @Before
    fun setUp() {
        useCase = DeleteKitUseCase(repository)
    }

    @Test
    fun `should_returnSuccess_when_deletionSucceeds`() = runTest {
        coEvery { repository.deleteKit(any()) } returns Result.success(Unit)

        val result = useCase("kit-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_deletionFails`() = runTest {
        coEvery { repository.deleteKit(any()) } returns Result.failure(Exception("Ошибка"))

        val result = useCase("kit-1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_passKitIdToRepository`() = runTest {
        coEvery { repository.deleteKit("kit-99") } returns Result.success(Unit)

        useCase("kit-99")

        coVerify(exactly = 1) { repository.deleteKit("kit-99") }
    }
}

class RefreshInviteCodeUseCaseTest {

    private val repository = mockk<KitRepository>()
    private lateinit var useCase: RefreshInviteCodeUseCase

    @Before
    fun setUp() {
        useCase = RefreshInviteCodeUseCase(repository)
    }

    @Test
    fun `should_returnSuccess_when_refreshSucceeds`() = runTest {
        coEvery { repository.refreshInviteCode(any()) } returns Result.success("NEWCODE1")

        val result = useCase("kit-1")

        assertTrue(result.isSuccess)
        assertEquals("NEWCODE1", result.getOrNull())
    }

    @Test
    fun `should_returnFailure_when_refreshFails`() = runTest {
        coEvery { repository.refreshInviteCode(any()) } returns Result.failure(Exception("Ошибка"))

        val result = useCase("kit-1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_passKitIdToRepository`() = runTest {
        coEvery { repository.refreshInviteCode("kit-5") } returns Result.success("CODE5555")

        useCase("kit-5")

        coVerify(exactly = 1) { repository.refreshInviteCode("kit-5") }
    }
}

class UpdateKitUseCaseTest {

    private val repository = mockk<KitRepository>()
    private lateinit var useCase: UpdateKitUseCase

    @Before
    fun setUp() {
        useCase = UpdateKitUseCase(repository)
        coEvery { repository.updateKit(any(), any(), any(), any(), any(), any()) } returns Result.success(Unit)
    }

    @Test
    fun `should_returnSuccess_when_updateSucceeds`() = runTest {
        val result = useCase("kit-1", "Аптечка", "Дом", 0, KitType.PERSONAL, listOf("u"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_updateFails`() = runTest {
        coEvery { repository.updateKit(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Ошибка обновления"))

        val result = useCase("kit-1", "Аптечка", "Дом", 0, KitType.PERSONAL, listOf("u"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_passAllParamsToRepository`() = runTest {
        val userIds = listOf("user-1", "user-2")

        useCase("kit-7", "Семейная", "Ванная", 3, KitType.SHARED, userIds)

        coVerify(exactly = 1) {
            repository.updateKit("kit-7", "Семейная", "Ванная", 3, KitType.SHARED, userIds)
        }
    }
}

class SetKitArchivedUseCaseTest {

    private val repository = mockk<KitRepository>()
    private lateinit var useCase: SetKitArchivedUseCase

    @Before
    fun setUp() {
        useCase = SetKitArchivedUseCase(repository)
        coEvery { repository.setArchived(any(), any(), any()) } returns Result.success(Unit)
    }

    @Test
    fun `should_returnSuccess_when_archiveSucceeds`() = runTest {
        val result = useCase("kit-1", "user-1", true)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_archiveFails`() = runTest {
        coEvery { repository.setArchived(any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        val result = useCase("kit-1", "user-1", true)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should_passAllParamsToRepository`() = runTest {
        useCase("kit-3", "user-7", false)

        coVerify(exactly = 1) { repository.setArchived("kit-3", "user-7", false) }
    }
}

class GetKitsUseCaseTest {

    private val kitRepository = mockk<KitRepository>()
    private val authRepository = mockk<AuthRepository>()
    private lateinit var useCase: GetKitsUseCase

    private val fakeKit = Kit(id = "kit-1", name = "Аптечка", type = KitType.PERSONAL, userIds = listOf("u"))

    @Before
    fun setUp() {
        useCase = GetKitsUseCase(kitRepository, authRepository)
    }

    @Test
    fun `should_returnKitsFlow_when_userAuthenticated`() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"
        every { kitRepository.getKits("user-1") } returns flowOf(listOf(fakeKit))

        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals("kit-1", result[0].id)
    }

    @Test
    fun `should_returnEmptyFlow_when_userNotAuthenticated`() = runTest {
        every { authRepository.getCurrentUserId() } returns null

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should_passUserIdToRepository_when_authenticated`() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-42"
        every { kitRepository.getKits("user-42") } returns flowOf(emptyList())

        useCase()

        verify(exactly = 1) { kitRepository.getKits("user-42") }
    }
}

class ObserveKitUseCaseTest {

    private val repository = mockk<KitRepository>()
    private lateinit var useCase: ObserveKitUseCase

    @Before
    fun setUp() {
        useCase = ObserveKitUseCase(repository)
    }

    @Test
    fun `should_returnFlowFromRepository`() = runTest {
        val kit = Kit(id = "kit-1", name = "Аптечка", type = KitType.PERSONAL, userIds = listOf("u"))
        every { repository.observeKit("kit-1") } returns flowOf(kit)

        val result = useCase("kit-1").first()

        assertEquals("kit-1", result?.id)
    }

    @Test
    fun `should_returnNullFlow_when_kitNotFound`() = runTest {
        every { repository.observeKit(any()) } returns flowOf(null)

        val result = useCase("kit-1").first()

        assertNull(result)
    }

    @Test
    fun `should_passKitIdToRepository`() = runTest {
        every { repository.observeKit("kit-99") } returns flowOf(null)

        useCase("kit-99")

        verify(exactly = 1) { repository.observeKit("kit-99") }
    }
}
