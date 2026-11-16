package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local citizen voting operations.
 * Enables 100% offline vote casting with status tracking for Firestore cloud sync.
 */
@Dao
interface CitizenVoteDao {

    @Query("SELECT * FROM citizen_votes")
    fun getAllVotesFlow(): Flow<List<CitizenVoteEntity>>

    @Query("SELECT * FROM citizen_votes WHERE pollId = :pollId LIMIT 1")
    fun getVoteForPollFlow(pollId: String): Flow<CitizenVoteEntity?>

    @Query("SELECT * FROM citizen_votes WHERE pollId = :pollId LIMIT 1")
    suspend fun getVoteForPoll(pollId: String): CitizenVoteEntity?

    @Query("SELECT * FROM citizen_votes WHERE syncedToCloud = 0")
    suspend fun getUnsyncedVotes(): List<CitizenVoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: CitizenVoteEntity)

    @Query("UPDATE citizen_votes SET syncedToCloud = 1 WHERE pollId = :pollId")
    suspend fun markAsSynced(pollId: String)

    @Query("DELETE FROM citizen_votes WHERE pollId = :pollId")
    suspend fun deleteVote(pollId: String)

    @Query("SELECT COUNT(*) FROM citizen_votes")
    suspend fun getTotalVotesCount(): Int
}
