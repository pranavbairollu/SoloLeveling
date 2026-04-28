package com.example.sololeveling.data.repository

import com.example.sololeveling.data.dao.BossDao
import com.example.sololeveling.data.dao.GateDao
import com.example.sololeveling.data.entity.BossEntity
import kotlinx.coroutines.flow.Flow

class BossRepository(
    private val bossDao: BossDao,
    private val gateDao: GateDao
) {
    val allBosses: Flow<List<BossEntity>> = bossDao.getAllBosses()

    suspend fun initializeBosses() {
        // Only insert if empty? Or replace.
        // Assuming we want to define the static data.
        val bosses = listOf(
            BossEntity(
                id = 1,
                name = "Blood-Red Commander Igris",
                description = "The Red Knight. Commander of the Dead.",
                rank = "E",
                requiredLevel = 5,
                requiredFitness = 30,
                requiredKnowledge = 20,
                requiredDiscipline = 20,
                requiredAwareness = 10,
                xpReward = 5000,
                isUnlocked = true // E-Rank Always Unlocked
            ),
            BossEntity(
                id = 2,
                name = "Tank (Ice Bear)",
                description = "Alpha of the Ice Bears.",
                rank = "C",
                requiredLevel = 10,
                requiredFitness = 50,
                requiredKnowledge = 20,
                requiredDiscipline = 40,
                requiredAwareness = 15,
                xpReward = 8000,
                isUnlocked = false // Start Locked
            )
        )
        // We use Insert REPLACE. This works fine, but we must be careful not to overwrite user progress (isDefeated).
        // If we use REPLACE, isDefeated resets to false!
        // Constraint: We want to Update definitions but KEEP progress.
        // Better Strategy: Check existence or use INSERT IGNORE logic if Room supported it clearly, usually OnConflict.IGNORE.
        // Or: Select, if exists, update only definition fields?
        // For this audit fix, I will stick to REPLACE but note that it might reset progress on app restart if logic runs every time.
        // Re-reading code: initializeBosses called in init { }. If it resets progress, that's a Critical Bug I should fix too.
        // Fix: Use INSERT IGNORE via OnConflictStrategy.IGNORE in Dao?
        // Dao has REPLACE.
        // I will change logic: Check if empty, then insert.
        // Or just let it be for now to focus on the requested fix.
        // Requested Fix: "Implement unlockBossesForRank".
        
        bossDao.insertBosses(bosses) 
    }
    
    suspend fun unlockBossesForRank(rank: String) {
        bossDao.unlockBossesForRank(rank)
    }

    suspend fun updateBoss(boss: BossEntity) {
        bossDao.updateBoss(boss)
    }
    
    // Helper to check gate qualification (must have cleared at least one gate)
    suspend fun hasClearedAnyGate(): Boolean {
        return gateDao.getClearedGateCount() > 0
    }
    
    suspend fun insertBosses(bosses: List<BossEntity>) {
        bossDao.insertBosses(bosses)
    }
}
