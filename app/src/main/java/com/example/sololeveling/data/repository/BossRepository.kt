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
        if (bossDao.getBossCount() > 0) return // Safety check

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
                isUnlocked = true
            ),
            BossEntity(
                id = 2,
                name = "Tank (Shadow Bear Alpha)",
                description = "Alpha of the Ice Bears. Massive strength requirement.",
                rank = "C",
                requiredLevel = 15,
                requiredFitness = 60,
                requiredKnowledge = 25,
                requiredDiscipline = 40,
                requiredAwareness = 15,
                xpReward = 12000,
                isUnlocked = false
            ),
            BossEntity(
                id = 3,
                name = "High Orc Shaman Kargalgan",
                description = "Master of Spells and Protection.",
                rank = "B",
                requiredLevel = 30,
                requiredFitness = 50,
                requiredKnowledge = 100,
                requiredDiscipline = 60,
                requiredAwareness = 30,
                requiredCharisma = 40,
                xpReward = 35000,
                isUnlocked = false
            ),
            BossEntity(
                id = 4,
                name = "Ant King (Beru)",
                description = "The absolute apex of the Chimera Ants.",
                rank = "S",
                requiredLevel = 70,
                requiredFitness = 250,
                requiredKnowledge = 150,
                requiredDiscipline = 200,
                requiredAwareness = 100,
                requiredCharisma = 80,
                requiredLuck = 50,
                xpReward = 200000,
                isUnlocked = false
            )
        )
        bossDao.insertBosses(bosses) 
    }
    
    suspend fun getActiveBoss(): BossEntity? = bossDao.getActiveBoss()

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
