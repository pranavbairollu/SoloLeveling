package com.example.sololeveling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sololeveling.data.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quest_table WHERE date = :date ORDER BY id ASC")
    fun getQuestsForDate(date: Long): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)
    
    @Query("DELETE FROM quest_table WHERE date < :date")
    suspend fun deleteOldQuests(date: Long)
    
    @Query("SELECT COUNT(*) FROM quest_table WHERE date = :date AND isCompleted = 0")
    suspend fun getIncompleteCount(date: Long): Int

    @Query("SELECT COUNT(*) FROM quest_table WHERE date = :date")
    suspend fun getQuestCountForDate(date: Long): Int

    @Query("SELECT * FROM quest_table WHERE date = :date")
    suspend fun getQuestsForDateSync(date: Long): List<QuestEntity>

    @Query("SELECT COUNT(*) FROM quest_table WHERE date = :date AND isCompleted = 0")
    suspend fun getIncompleteQuestCountForDate(date: Long): Int
}
