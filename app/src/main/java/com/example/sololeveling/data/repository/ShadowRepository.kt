package com.example.sololeveling.data.repository

import com.example.sololeveling.data.dao.QuestDao
import com.example.sololeveling.data.dao.ShadowDao
import com.example.sololeveling.data.entity.BossEntity
import com.example.sololeveling.data.entity.QuestEntity
import com.example.sololeveling.data.entity.ShadowEntity
import com.example.sololeveling.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ShadowRepository(
    private val shadowDao: ShadowDao,
    private val questDao: QuestDao
) {
    val allShadows: Flow<List<ShadowEntity>> = shadowDao.getAllShadows()

    suspend fun extractShadow(boss: BossEntity) {
        // Create Shadow based on Boss
        val shadowName = "SHADOW ${boss.name.uppercase()}"
        val type = when {
            boss.requiredFitness > boss.requiredKnowledge -> "STR"
            boss.requiredKnowledge > boss.requiredFitness -> "INT"
            else -> "DISC"
        }
        
        val shadow = ShadowEntity(
            name = shadowName,
            sourceBossId = boss.id,
            boostedStat = type,
            isActive = false,
            loyaltyLevel = 1
        )
        shadowDao.insertShadow(shadow)
    }

    suspend fun generateDailyShadowQuests() {
        val activeShadows = shadowDao.getActiveShadowsSync()
        val today = getTodayDate()
        
        // Count existing shadow quests for today to prevent dupes (Logic simplified: rely on conflicting ID or check Count)
        // Since we don't have unique IDs for daily gen easily without UUID or careful logic, 
        // we'll check if we already have quests with "SHADOW" type for today.
        
        // We really need a way to know if we generated them.
        // Let's assume this is called once/day.
        // Better: Check if `questDao.getQuestsForDate(today)` contains any with title starting with "[SHADOW]"
        val todayQuests = questDao.getQuestsForDateSync(today) // Assuming this method exists or we add it
        // If query not available, we might skip duplication check for this proto-phase OR add query.
        // User said "Do not refactor... Quest systems".
        // I'll add query to QuestDao if needed, strictly additive.
        
        if (todayQuests.any { it.title.startsWith("[SHADOW]") }) {
            return // Already generated
        }

        val newQuests = activeShadows.map { shadow ->
            QuestEntity(
                title = "[SHADOW] ${shadow.name} Training",
                linkedStat = "SHADOW_${shadow.boostedStat}", // Special type for visuals
                xpReward = 500, // Bonus XP
                statReward = 1,
                date = today,
                isCompleted = false
            )
        }
        
        if (newQuests.isNotEmpty()) {
            questDao.insertQuests(newQuests)
        }
    }
    
    suspend fun updateShadow(shadow: ShadowEntity) {
        shadowDao.updateShadow(shadow)
    }
    
    suspend fun getShadowByBossId(id: Int): ShadowEntity? {
        return shadowDao.getShadowByBossId(id)
    }
    
    suspend fun processQuestCompletion(quest: QuestEntity) {
        if (quest.linkedStat.startsWith("SHADOW_")) {
            // Find which shadow? 
            // Name is "[SHADOW] SHADOW IGRIS Training"
            // We can match loosely or look up all active shadows.
            val shadows = shadowDao.getActiveShadowsSync()
            // Simplified: Increase ALL active shadows linked to this type?
            // Or extract name.
            // Let's rely on name matching for now.
            val shadowName = quest.title.substringAfter("[SHADOW] ").substringBefore(" Training")
            // shadowName = "SHADOW IGRIS"
            
            val shadow = shadows.find { it.name == shadowName }
            if (shadow != null) {
                shadowDao.updateShadow(shadow.copy(loyaltyLevel = shadow.loyaltyLevel + 1))
            }
        }
    }
    
    suspend fun processDailyFailure(user: UserEntity) {
        if (user.isMonarch) return

        // Check incomplete shadow quests for that date
        val failedQuests = questDao.getQuestsForDateSync(user.lastActiveDate).filter { !it.isCompleted && it.linkedStat.startsWith("SHADOW_") }
        
        if (failedQuests.isNotEmpty()) {
            val shadows = shadowDao.getActiveShadowsSync()
            failedQuests.forEach { quest ->
                val shadowName = quest.title.substringAfter("[SHADOW] ").substringBefore(" Training")
                val shadow = shadows.find { it.name == shadowName }
                if (shadow != null) {
                    val newLoyalty = shadow.loyaltyLevel - 1
                    if (newLoyalty <= 0) {
                        shadowDao.updateShadow(shadow.copy(loyaltyLevel = 0, isActive = false))
                        // "SHADOW HAS ABANDONED YOU" handled by UI observing this change ideally.
                    } else {
                        shadowDao.updateShadow(shadow.copy(loyaltyLevel = newLoyalty))
                    }
                }
            }
        }
    }

    suspend fun reduceAllShadowsLoyalty() {
        val shadows = shadowDao.getActiveShadowsSync()
        shadows.forEach { shadow ->
            val newLoyalty = shadow.loyaltyLevel - 1
            if (newLoyalty <= 0) {
                 shadowDao.updateShadow(shadow.copy(loyaltyLevel = 0, isActive = false))
            } else {
                 shadowDao.updateShadow(shadow.copy(loyaltyLevel = newLoyalty))
            }
        }
    }

    private fun getTodayDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    suspend fun insertShadows(shadows: List<ShadowEntity>) {
        shadows.forEach { shadowDao.insertShadow(it) }
    }
}
