package com.asha.worker.ai.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The persistent memory of the app. A [Household] has many [Patient]s; each patient
 * accumulates [Visit]s, scheduled [Immunization]s, and open [FollowUpTask]s.
 * Everything is local-only (encrypted at rest in Phase 13).
 */

@Entity(tableName = "households", indices = [Index("phoneticKey")])
data class Household(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val headName: String,
    val phoneticKey: String,
    val locationTag: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "patients",
    foreignKeys = [ForeignKey(
        entity = Household::class,
        parentColumns = ["id"], childColumns = ["householdId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("householdId"), Index("phoneticKey")]
)
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val householdId: Long,
    val name: String,
    val phoneticKey: String,
    val gender: String? = null,          // "M" | "F" | null
    val dobMillis: Long? = null,
    val pregnant: Boolean = false,
    val lmpMillis: Long? = null,         // last menstrual period (pregnancy tracking)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "visits",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"], childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class Visit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val symptomCode: String? = null,
    val durationDays: Int? = null,
    val guidance: String? = null,
    val referral: Boolean = false,
    val followUpMillis: Long? = null,
    val notes: String? = null
)

@Entity(
    tableName = "immunizations",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"], childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class Immunization(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val vaccineCode: String,
    val dueMillis: Long,
    val givenMillis: Long? = null,
    val status: String = STATUS_DUE
) {
    companion object {
        const val STATUS_DUE = "DUE"
        const val STATUS_GIVEN = "GIVEN"
    }
}

@Entity(
    tableName = "followups",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"], childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class FollowUpTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val type: String,                    // FollowUpType.*
    val dueMillis: Long,
    val reason: String,
    val status: String = STATUS_OPEN
) {
    companion object {
        const val STATUS_OPEN = "OPEN"
        const val STATUS_DONE = "DONE"
    }
}

object FollowUpType {
    const val SYMPTOM = "SYMPTOM_FOLLOWUP"
    const val IMMUNIZATION = "IMMUNIZATION"
    const val ANC = "ANC"
}
