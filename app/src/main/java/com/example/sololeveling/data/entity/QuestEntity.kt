package com.example.sololeveling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_table")
data class QuestEntity(
@PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    
    @androidx.room.ColumnInfo(name = "type")
    val linkedStat: String, // "Fitness", "Knowledge", etc. (Mapped to old 'type')
    
    // Requirements (Editable)
    val requirementTarget: Int = 1,
    val requirementUnit: String = "Count", // "Minutes", "Pages", "Reps"
    val difficulty: String = "E", // E, D, C, B, A, S
    
    val currentProgress: Int = 0, // Track progress
    
    val xpReward: Int = 10,
    val statReward: Int = 1, // +1 to relevant stat
    
    val isCompleted: Boolean = false,
    val date: Long, 
    val isPenalty: Boolean = false,
    
    // Class Generation
    val generatedByClass: String? = null // e.g., "Student", "Athlete"
)
