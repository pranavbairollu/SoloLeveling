package com.example.sololeveling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
@PrimaryKey
    val id: Int = 1, // Single user system
    val username: String = "Player",
    val level: Int = 1,
    val currentXP: Long = 0,
    val rank: String = "E",
    
    // Stats (Renamed & Expanded)
    @androidx.room.ColumnInfo(name = "strength") 
    val fitness: Int = 10,       // STR -> Fitness
    @androidx.room.ColumnInfo(name = "intelligence") 
    val knowledge: Int = 10,     // INT -> Knowledge
    
    // Existing names match request intent, but ensuring clarity
    val discipline: Int = 10,    // VIT -> Discipline
    val awareness: Int = 10,     // AGI -> Awareness
    
    // New Stats
    val charisma: Int = 10,      // CHR -> Charisma
    val luck: Int = 10,          // LUK -> Luck
    
    // Survival State (HP)
    val endurance: Int = 100,
    val maxEndurance: Int = 100,
    
    val lastActiveDate: Long = System.currentTimeMillis(),
    val monarchModeEnabled: Boolean = true,
    val penaltyEndTime: Long = 0L, // Timestamp when penalty expires. 0 if inactive.
    
    // Rank Promotion Criteria
    val hasClearedGateSincePromotion: Boolean = false,
    val hasDefeatedBossSincePromotion: Boolean = false,
    
    // Monarch Authority
    val isMonarch: Boolean = false,
    
    // Stats & Settings
    val unspentPoints: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    
    // Penalty Details
    val penaltyStatReduction: Int = 0, // Amount to subtract from all major stats during penalty
    
    // Onboarding Flag
    val onboardingCompleted: Boolean = false
)
