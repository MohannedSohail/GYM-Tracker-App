package com.gymtracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymtracker.app.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getActiveParticipants(): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants ORDER BY createdAt ASC")
    fun getAllParticipants(): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE id = :id LIMIT 1")
    suspend fun getParticipantById(id: Long): ParticipantEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertParticipant(participant: ParticipantEntity): Long

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity)

    @Query("UPDATE participants SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteParticipant(id: Long)
}
