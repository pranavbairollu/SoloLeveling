package com.example.sololeveling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boss_table")
data class BossEntity(
@PrimaryKey
    val id: Int,
    val name: String, // Editable
    val description: String,
    val rank: String = "E", // E-S
    
    val requiredLevel: Int,
    
    // Required Stats (Renamed & Expanded)
    @androidx.room.ColumnInfo(name = "requiredStrength")
    val requiredFitness: Int, // STR -> Fitness
    
    @androidx.room.ColumnInfo(name = "requiredIntelligence")
    val requiredKnowledge: Int, // INT -> Knowledge
    
    val requiredDiscipline: Int, // VIT
    val requiredAwareness: Int = 0, // AGI (New requirement field if not previously present, or map if exists. Previous file checking showed missing requiredAwareness, so adding fresh.)
    
    val requiredCharisma: Int = 0,
    val requiredLuck: Int = 0,
    
    val xpReward: Int,
    
    val isDefeated: Boolean = false,
    val isUnlocked: Boolean = false,
    val dueDate: Long = 0L, // Critical Date
    val cooldownUntil: Long = 0L // Timestamp until boss is available again if failed
)
