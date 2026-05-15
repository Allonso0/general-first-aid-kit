package com.example.general_first_aid_kit.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.data.local.AppDatabase
import com.example.general_first_aid_kit.data.local.entity.KitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KitDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: KitDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.kitDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_and_observeById_returnsInsertedKit() = runTest {
        val kit = fakeKit("kit-1")
        dao.upsert(kit)

        val result = dao.observeById("kit-1").first()

        assertEquals(kit, result)
    }

    @Test
    fun upsert_replacesExistingKit_when_sameIdUpserted() = runTest {
        dao.upsert(fakeKit("kit-1", name = "Старая"))
        dao.upsert(fakeKit("kit-1", name = "Новая"))

        val result = dao.observeById("kit-1").first()

        assertEquals("Новая", result?.name)
    }

    @Test
    fun upsertAll_insertsMultipleKits() = runTest {
        val kits = listOf(fakeKit("kit-1"), fakeKit("kit-2"), fakeKit("kit-3"))
        dao.upsertAll(kits)

        assertNotNull(dao.getById("kit-1"))
        assertNotNull(dao.getById("kit-2"))
        assertNotNull(dao.getById("kit-3"))
    }

    @Test
    fun observeById_returnsNull_when_kitDoesNotExist() = runTest {
        val result = dao.observeById("nonexistent").first()

        assertNull(result)
    }

    @Test
    fun getById_returnsKit_when_exists() = runTest {
        dao.upsert(fakeKit("kit-42"))

        val result = dao.getById("kit-42")

        assertNotNull(result)
        assertEquals("kit-42", result?.id)
    }

    @Test
    fun getById_returnsNull_when_notExists() = runTest {
        val result = dao.getById("ghost")

        assertNull(result)
    }

    @Test
    fun observeByUserId_returnsKitsContainingUser() = runTest {
        dao.upsert(fakeKit("kit-1", userIds = listOf("user-1", "user-2")))
        dao.upsert(fakeKit("kit-2", userIds = listOf("user-2", "user-3")))
        dao.upsert(fakeKit("kit-3", userIds = listOf("user-3")))

        val result = dao.observeByUserId("user-1").first()

        assertEquals(1, result.size)
        assertEquals("kit-1", result[0].id)
    }

    @Test
    fun observeByUserId_returnsEmpty_when_noMatch() = runTest {
        dao.upsert(fakeKit("kit-1", userIds = listOf("user-2")))

        val result = dao.observeByUserId("user-1").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeByUserId_doesNotMatchPartialUserId() = runTest {
        dao.upsert(fakeKit("kit-1", userIds = listOf("user-10")))

        val result = dao.observeByUserId("user-1").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeByUserId_returnsMultipleKits_when_userIsInAll() = runTest {
        dao.upsert(fakeKit("kit-1", userIds = listOf("user-1")))
        dao.upsert(fakeKit("kit-2", userIds = listOf("user-1", "user-2")))

        val result = dao.observeByUserId("user-1").first()

        assertEquals(2, result.size)
    }

    @Test
    fun deleteById_removesKit() = runTest {
        dao.upsert(fakeKit("kit-1"))

        dao.deleteById("kit-1")

        assertNull(dao.getById("kit-1"))
    }

    @Test
    fun deleteById_returnsOneRowDeleted() = runTest {
        dao.upsert(fakeKit("kit-1"))

        val rowsDeleted = dao.deleteById("kit-1")

        assertEquals(1, rowsDeleted)
    }

    @Test
    fun deleteAll_removesAllKits() = runTest {
        dao.upsertAll(listOf(fakeKit("kit-1"), fakeKit("kit-2")))

        dao.deleteAll()

        assertNull(dao.getById("kit-1"))
        assertNull(dao.getById("kit-2"))
    }

    private fun fakeKit(
        id: String,
        name: String = "Аптечка",
        userIds: List<String> = listOf("user-1")
    ) = KitEntity(
        id = id,
        name = name,
        location = "",
        colorIndex = 0,
        ownerId = "user-1",
        userIds = userIds,
        type = "PERSONAL",
        archivedUserIds = emptyList(),
        inviteCode = null,
        updatedAt = 1000L
    )
}
