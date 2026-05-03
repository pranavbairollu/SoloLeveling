package com.example.sololeveling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shadow_table")
data class ShadowEntity(
@PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sourceBossId: Int,
    
    @androidx.room.ColumnInfo(name = "linkedQuestType")
    val boostedStat: String, // "Fitness", "Knowledge" etc.
    
    val boostMultiplier: Double = 1.0,
    val unlockCondition: String = "",
    
    val isActive: Boolean = true,
    val loyaltyLevel: Int = 1,
    val isResurrected: Boolean = false
)
