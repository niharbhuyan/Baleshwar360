package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val odiaName: String,
    val number: String,
    val category: String,
    val is24x7: Boolean = true,
    val description: String = "",
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY category ASC, name ASC")
    fun getAllEmergencyContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY category ASC, name ASC")
    suspend fun getAllEmergencyContactsSync(): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE category = :category ORDER BY name ASC")
    fun getEmergencyContactsByCategory(category: String): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContacts(contacts: List<EmergencyContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts")
    suspend fun clearAllEmergencyContacts()

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getCount(): Int
}
