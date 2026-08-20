package com.asha.worker.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Household::class, Patient::class, Visit::class, Immunization::class, FollowUpTask::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
    abstract fun patientDao(): PatientDao
    abstract fun visitDao(): VisitDao
    abstract fun immunizationDao(): ImmunizationDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    // Encrypt the database at rest (health PII) with a keystore-backed key.
                    SQLiteDatabase.loadLibs(context.applicationContext)
                    val factory = SupportFactory(DbKey.getOrCreate(context.applicationContext))
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "asha_health_db"
                    )
                        .openHelperFactory(factory)
                        // Schema is still pre-1.0; destructive is acceptable until a real
                        // migration path is needed post-launch.
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { INSTANCE = it }
                }
            }
    }
}
