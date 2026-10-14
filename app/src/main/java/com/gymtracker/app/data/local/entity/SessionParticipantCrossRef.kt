package com.gymtracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "session_participants",
    primaryKeys = ["sessionId", "participantId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("sessionId"),
        Index("participantId")
    ]
)
data class SessionParticipantCrossRef(
    val sessionId: Long,
    val participantId: Long
)
