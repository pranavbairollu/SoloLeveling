package com.example.sololeveling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gate_table")
data class GateEntity(
@PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val durationDays: Int, // Legacy
    val durationMillis: Long = 0L, // New precise duration
    val requiredLevel: Int,
    val xpReward: Int,
    
    // State
    val isActive: Boolean = false,
    val isCleared: Boolean = false,
    val isFailed: Boolean = false,
    
    // Conditions
    val failCondition: String? = null,
    
    // Timers
    val startTimestamp: Long = 0L,
    val endTimestamp: Long = 0L,
    
    // Progress
    val daysCompleted: Int = 0,
    val cooldownEnd: Long = 0L // If failed, locked until this time
)
