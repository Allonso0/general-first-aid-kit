package com.example.general_first_aid_kit.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.data.local.AppDatabase
import com.example.general_first_aid_kit.data.local.entity.KitEntity
import com.example.general_first_aid_kit.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var kitDao: KitDao
    private lateinit var dao: MedicationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        kitDao = db.kitDao()
        dao = db.medicationDao()
        runBlocking {
            kitDao.upsert(fakeKit("kit-1"))
            kitDao.upsert(fakeKit("kit-2"))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_and_observeByKitId_returnsMedication() = runTest {
        val med = fakeMedication("med-1", kitId = "kit-1")
        dao.upsert(med)

        val result = dao.observeByKitId("kit-1").first()

        assertEquals(1, result.size)
        assertEquals(med, result[0])
    }

    @Test
    fun upsert_replacesExistingMedication_when_sameIdUpserted() = runTest {
        dao.upsert(fakeMedication("med-1", name = "Аспирин", kitId = "kit-1"))
        dao.upsert(fakeMedication("med-1", name = "Ибупрофен", kitId = "kit-1"))

        val result = dao.observeByKitId("kit-1").first()

        assertEquals(1, result.size)
        assertEquals("Ибупрофен", result[0].name)
    }

    @Test
    fun upsertAll_insertsMultipleMedications() = runTest {
        val meds = listOf(
            fakeMedication("med-1", kitId = "kit-1"),
            fakeMedication("med-2", kitId = "kit-1"),
            fakeMedication("med-3", kitId = "kit-2")
        )
        dao.upsertAll(meds)

        val kit1Meds = dao.observeByKitId("kit-1").first()
        val kit2Meds = dao.observeByKitId("kit-2").first()

        assertEquals(2, kit1Meds.size)
        assertEquals(1, kit2Meds.size)
    }

    @Test
    fun observeByKitId_returnsOnlyMedicationsForThatKit() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))
        dao.upsert(fakeMedication("med-2", kitId = "kit-2"))

        val result = dao.observeByKitId("kit-1").first()

        assertEquals(1, result.size)
        assertEquals("med-1", result[0].id)
    }

    @Test
    fun observeByKitId_returnsEmpty_when_noMedicationsInKit() = runTest {
        val result = dao.observeByKitId("kit-1").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeAll_returnsAllMedicationsAcrossKits() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))
        dao.upsert(fakeMedication("med-2", kitId = "kit-2"))

        val result = dao.observeAll().first()

        assertEquals(2, result.size)
    }

    @Test
    fun observeById_returnsMedication_when_exists() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))

        val result = dao.observeById("kit-1", "med-1").first()

        assertEquals("med-1", result?.id)
    }

    @Test
    fun observeById_returnsNull_when_notExists() = runTest {
        val result = dao.observeById("kit-1", "ghost").first()

        assertNull(result)
    }

    @Test
    fun deleteById_removesMedication() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))

        dao.deleteById("med-1")

        val result = dao.observeByKitId("kit-1").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteByKitId_removesAllMedicationsInKit() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))
        dao.upsert(fakeMedication("med-2", kitId = "kit-1"))

        dao.deleteByKitId("kit-1")

        val result = dao.observeByKitId("kit-1").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteByKitId_doesNotRemoveMedicationsOfOtherKits() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))
        dao.upsert(fakeMedication("med-2", kitId = "kit-2"))

        dao.deleteByKitId("kit-1")

        val kit2Result = dao.observeByKitId("kit-2").first()
        assertEquals(1, kit2Result.size)
    }

    @Test
    fun cascadeDelete_removesMedications_when_parentKitDeleted() = runTest {
        dao.upsert(fakeMedication("med-1", kitId = "kit-1"))

        kitDao.deleteById("kit-1")

        val result = dao.observeByKitId("kit-1").first()
        assertTrue(result.isEmpty())
    }

    private fun fakeKit(id: String) = KitEntity(
        id = id,
        name = "Аптечка",
        location = "",
        colorIndex = 0,
        ownerId = "user-1",
        userIds = listOf("user-1"),
        type = "PERSONAL",
        archivedUserIds = emptyList(),
        inviteCode = null,
        updatedAt = 1000L
    )

    private fun fakeMedication(
        id: String,
        name: String = "Аспирин",
        kitId: String
    ) = MedicationEntity(
        id = id,
        kitId = kitId,
        name = name,
        expirationDate = System.currentTimeMillis() + 86400000L,
        quantity = 10,
        unit = "таб",
        category = "Анальгетик",
        description = "",
        photoUrl = null,
        localPhotoUri = null,
        updatedAt = 1000L
    )
}
