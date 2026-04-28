package com.example.sololeveling.data.repository

import com.example.sololeveling.data.dao.QuestDao
import com.example.sololeveling.data.entity.QuestEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class QuestRepository(private val questDao: QuestDao) {

    fun getTodayQuests(): Flow<List<QuestEntity>> {
        val today = getTodayDate()
        return questDao.getQuestsForDate(today)
    }

    suspend fun checkAndGenerateQuests() {
        val today = getTodayDate()
        // Check if we have quests? Count is better?
        // Actually flow observes it. But we need to ensure they exist.
        // We can check count.
        val count = questDao.getIncompleteCount(today) // Wait, this logic is "incomplete".
        // Use a direct query or just trust that if list is empty we generate.
        // But Flow is async. Let's assume we do a one-shot check.
        // Or simpler: Insert with IGNORE? No, REPLACE.
        // We need a check.
        // Let's rely on ViewModel to call this.
    }
    
    suspend fun generateDailyQuestsIfNeeded(userLevel: Int, userClass: String = "Student") {
         val today = getTodayDate()
         val count = questDao.getQuestCountForDate(today)
         if (count == 0) {
             createDailyQuests(userLevel, userClass)
         }
    }
    
    // Supports Red Gate injection (flag only, logic later)
    private var forceRedGate = false 

    private suspend fun createDailyQuests(level: Int, userClass: String) {
        val today = getTodayDate()
        val isRedGate = forceRedGate // Placeholder for future logic
        
        val quests = mutableListOf<QuestEntity>()
        
        // Base scaling
        val baseRep = (10 + level * 2).coerceAtMost(100)
        val baseMin = (5 + level).coerceAtMost(60)
        
        when (userClass) {
            "Athlete" -> {
                quests.add(QuestEntity(title = "Morning Run", linkedStat = "Fitness", requirementTarget = baseMin * 2, requirementUnit = "Minutes", difficulty = "C", date = today, generatedByClass = "Athlete"))
                quests.add(QuestEntity(title = "Strength Training", linkedStat = "Fitness", requirementTarget = baseRep * 2, requirementUnit = "Reps", difficulty = "C", date = today, generatedByClass = "Athlete"))
                quests.add(QuestEntity(title = "Focus Meditation", linkedStat = "Discipline", requirementTarget = baseMin, requirementUnit = "Minutes", difficulty = "D", date = today, generatedByClass = "Athlete"))
            }
            "Executive" -> {
                 quests.add(QuestEntity(title = "Strategic Reading", linkedStat = "Knowledge", requirementTarget = baseMin, requirementUnit = "Pages", difficulty = "D", date = today, generatedByClass = "Executive"))
                 quests.add(QuestEntity(title = "Networking / Social", linkedStat = "Charisma", requirementTarget = 1, requirementUnit = "Event", difficulty = "B", date = today, generatedByClass = "Executive")) 
                 quests.add(QuestEntity(title = "Deep Work Session", linkedStat = "Discipline", requirementTarget = baseMin * 2, requirementUnit = "Minutes", difficulty = "C", date = today, generatedByClass = "Executive"))
            }
            "Student", "Custom" -> { // Default to Student
                quests.add(QuestEntity(title = "Study Session", linkedStat = "Knowledge", requirementTarget = baseMin * 2, requirementUnit = "Minutes", difficulty = "C", date = today, generatedByClass = "Student"))
                quests.add(QuestEntity(title = "Daily Exercise", linkedStat = "Fitness", requirementTarget = baseRep, requirementUnit = "Reps", difficulty = "D", date = today, generatedByClass = "Student"))
                quests.add(QuestEntity(title = "Reading", linkedStat = "Knowledge", requirementTarget = 10, requirementUnit = "Pages", difficulty = "E", date = today, generatedByClass = "Student"))
            }
            else -> {
                // Fallback / General
                quests.add(QuestEntity(title = "Pushups", linkedStat = "Fitness", requirementTarget = baseRep, requirementUnit = "Reps", difficulty = "E", date = today))
                quests.add(QuestEntity(title = "Reading", linkedStat = "Knowledge", requirementTarget = 10, requirementUnit = "Pages", difficulty = "E", date = today))
                quests.add(QuestEntity(title = "Meditation", linkedStat = "Discipline", requirementTarget = baseMin, requirementUnit = "Minutes", difficulty = "E", date = today))
            }
        }
        
        // Red Gate Injection (Flag Only)
        if (isRedGate) {
            quests.add(QuestEntity(title = "SURVIVE: Red Gate", linkedStat = "Awareness", requirementTarget = 1, requirementUnit = "Survival", difficulty = "S", date = today, isPenalty = true))
        }

        questDao.insertQuests(quests)
    }
    
    suspend fun markQuestComplete(quest: QuestEntity) {
        val updated = quest.copy(isCompleted = true)
        questDao.updateQuest(updated)
    }

    private fun getTodayDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    suspend fun insertQuests(quests: List<QuestEntity>) {
        questDao.insertQuests(quests)
    }

    suspend fun hasIncompleteQuests(date: Long): Boolean {
        val count = questDao.getIncompleteCount(date)
        return count > 0
    }

    suspend fun getIncompleteCount(date: Long): Int {
        return questDao.getIncompleteCount(date)
    }
}
