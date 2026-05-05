package com.example.general_first_aid_kit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.general_first_aid_kit.data.local.converter.Converters
import com.example.general_first_aid_kit.data.local.dao.KitDao
import com.example.general_first_aid_kit.data.local.dao.MedicationDao
import com.example.general_first_aid_kit.data.local.dao.SyncOperationDao
import com.example.general_first_aid_kit.data.local.entity.KitEntity
import com.example.general_first_aid_kit.data.local.entity.MedicationEntity
import com.example.general_first_aid_kit.data.local.entity.SyncOperationEntity

@Database(
    entities = [KitEntity::class, MedicationEntity::class, SyncOperationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kitDao(): KitDao
    abstract fun medicationDao(): MedicationDao
    abstract fun syncOperationDao(): SyncOperationDao
}
