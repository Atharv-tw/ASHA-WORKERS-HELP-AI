package com.asha.worker.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HouseholdDao {
    @Insert suspend fun insert(h: Household): Long
    @Query("SELECT * FROM households WHERE phoneticKey = :key LIMIT 1")
    suspend fun findByPhoneticKey(key: String): Household?
    @Query("SELECT * FROM households WHERE id = :id")
    suspend fun getById(id: Long): Household?
    @Query("SELECT * FROM households ORDER BY headName")
    suspend fun getAll(): List<Household>
}

@Dao
interface PatientDao {
    @Insert suspend fun insert(p: Patient): Long
    @Update suspend fun update(p: Patient)
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getById(id: Long): Patient?
    @Query("SELECT * FROM patients WHERE householdId = :householdId")
    suspend fun forHousehold(householdId: Long): List<Patient>
    @Query("SELECT * FROM patients WHERE phoneticKey = :key")
    suspend fun findByPhoneticKey(key: String): List<Patient>
    @Query("SELECT * FROM patients ORDER BY name")
    suspend fun getAll(): List<Patient>
}

@Dao
interface VisitDao {
    @Insert suspend fun insert(v: Visit): Long
    @Query("SELECT * FROM visits WHERE patientId = :patientId ORDER BY timestamp DESC LIMIT 1")
    suspend fun lastForPatient(patientId: Long): Visit?
    @Query("SELECT * FROM visits WHERE patientId = :patientId ORDER BY timestamp DESC")
    suspend fun forPatient(patientId: Long): List<Visit>
    @Query("SELECT * FROM visits ORDER BY timestamp DESC")
    suspend fun getAll(): List<Visit>
}

@Dao
interface ImmunizationDao {
    @Insert suspend fun insertAll(items: List<Immunization>)
    @Query("SELECT * FROM immunizations WHERE patientId = :patientId ORDER BY dueMillis")
    suspend fun forPatient(patientId: Long): List<Immunization>
    @Query("SELECT * FROM immunizations WHERE status = 'DUE' AND dueMillis <= :by ORDER BY dueMillis")
    suspend fun dueBy(by: Long): List<Immunization>
    @Query("UPDATE immunizations SET status = 'GIVEN', givenMillis = :at WHERE id = :id")
    suspend fun markGiven(id: Long, at: Long)
}

@Dao
interface FollowUpDao {
    @Insert suspend fun insert(t: FollowUpTask): Long
    @Query("SELECT * FROM followups WHERE status = 'OPEN' AND dueMillis <= :by ORDER BY dueMillis")
    suspend fun openDueBy(by: Long): List<FollowUpTask>
    @Query("SELECT * FROM followups WHERE status = 'OPEN' ORDER BY dueMillis")
    suspend fun open(): List<FollowUpTask>
    @Query("UPDATE followups SET status = 'DONE' WHERE id = :id")
    suspend fun markDone(id: Long)
}
