package com.example.sololeveling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monarch_table")
data class MonarchEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton
    val title: String = "Monarch", // Editable, e.g., "Professor", "Valedictorian"
    val victoryCondition: String, // Description of how to win
    val requiredAggregateStats: Int, // Total stats needed
    val isCompleted: Boolean = false
)
