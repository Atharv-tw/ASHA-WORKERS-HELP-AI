package com.asha.worker.ai

import androidx.room.*

@Entity(tableName = "visit_records")
data class VisitRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val age: Int?,
    val symptom: String?,
    val durationDays: Int?,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VisitDao {
    @Insert
    suspend fun insert(record: VisitRecord)

    @Query("SELECT * FROM visit_records ORDER BY timestamp DESC")
    suspend fun getAll(): List<VisitRecord>
}

@Database(entities = [VisitRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "asha_health_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
