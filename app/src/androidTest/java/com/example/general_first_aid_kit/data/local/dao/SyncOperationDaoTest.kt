package com.example.general_first_aid_kit.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.data.local.AppDatabase
import com.example.general_first_aid_kit.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncOperationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SyncOperationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.syncOperationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_storesOperation() = runTest {
        val op = fakeOp("op-1")
        dao.insert(op)

        val result = dao.getAll()

        assertEquals(1, result.size)
        assertEquals(op, result[0])
    }

    @Test
    fun insert_ignoresDuplicate_when_sameIdInserted() = runTest {
        dao.insert(fakeOp("op-1", retryCount = 0))
        dao.insert(fakeOp("op-1", retryCount = 5))

        val result = dao.getAll()

        assertEquals(1, result.size)
        assertEquals(0, result[0].retryCount)
    }

    @Test
    fun getAll_returnsOperationsOrderedByCreatedAtAsc() = runTest {
        dao.insert(fakeOp("op-3", createdAt = 3000L))
        dao.insert(fakeOp("op-1", createdAt = 1000L))
        dao.insert(fakeOp("op-2", createdAt = 2000L))

        val result = dao.getAll()

        assertEquals("op-1", result[0].id)
        assertEquals("op-2", result[1].id)
        assertEquals("op-3", result[2].id)
    }

    @Test
    fun updateRetryCount_updatesCount() = runTest {
        dao.insert(fakeOp("op-1", retryCount = 0))

        dao.updateRetryCount("op-1", 3)

        val result = dao.getAll()
        assertEquals(3, result[0].retryCount)
    }

    @Test
    fun updateRetryCount_returnsOneRowAffected() = runTest {
        dao.insert(fakeOp("op-1"))

        val rows = dao.updateRetryCount("op-1", 2)

        assertEquals(1, rows)
    }

    @Test
    fun deleteById_removesOperation() = runTest {
        dao.insert(fakeOp("op-1"))
        dao.insert(fakeOp("op-2"))

        dao.deleteById("op-1")

        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals("op-2", result[0].id)
    }

    @Test
    fun deleteById_returnsOneRowDeleted() = runTest {
        dao.insert(fakeOp("op-1"))

        val rows = dao.deleteById("op-1")

        assertEquals(1, rows)
    }

    @Test
    fun deleteAllForKit_removesOperations_by_entityId() = runTest {
        dao.insert(fakeOp("op-1", entityId = "kit-1", kitId = null))
        dao.insert(fakeOp("op-2", entityId = "kit-2", kitId = null))

        dao.deleteAllForKit("kit-1")

        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals("op-2", result[0].id)
    }

    @Test
    fun deleteAllForKit_removesOperations_by_kitId() = runTest {
        dao.insert(fakeOp("op-1", entityId = "med-1", kitId = "kit-1"))
        dao.insert(fakeOp("op-2", entityId = "med-2", kitId = "kit-2"))

        dao.deleteAllForKit("kit-1")

        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals("op-2", result[0].id)
    }

    @Test
    fun deleteAllForKit_removesOperations_by_entityId_and_kitId() = runTest {
        dao.insert(fakeOp("op-1", entityId = "kit-1", kitId = null))
        dao.insert(fakeOp("op-2", entityId = "med-1", kitId = "kit-1"))
        dao.insert(fakeOp("op-3", entityId = "med-2", kitId = "kit-2"))

        dao.deleteAllForKit("kit-1")

        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals("op-3", result[0].id)
    }

    @Test
    fun deleteAll_removesAllOperations() = runTest {
        dao.insert(fakeOp("op-1"))
        dao.insert(fakeOp("op-2"))
        dao.insert(fakeOp("op-3"))

        dao.deleteAll()

        assertTrue(dao.getAll().isEmpty())
    }

    private fun fakeOp(
        id: String,
        entityId: String = "entity-1",
        kitId: String? = "kit-1",
        createdAt: Long = 1000L,
        retryCount: Int = 0
    ) = SyncOperationEntity(
        id = id,
        entityType = "KIT",
        operationType = "UPSERT",
        entityId = entityId,
        kitId = kitId,
        payload = "{}",
        localPhotoUri = null,
        createdAt = createdAt,
        retryCount = retryCount
    )
}
