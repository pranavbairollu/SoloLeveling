package com.example.sololeveling.data.repository

import com.example.sololeveling.data.dao.GateDao
import com.example.sololeveling.data.entity.GateEntity
import kotlinx.coroutines.flow.Flow

class GateRepository(private val gateDao: GateDao) {

    val allGates: Flow<List<GateEntity>> = gateDao.getAllGates()
    val activeGate: Flow<GateEntity?> = gateDao.getActiveGateFlow()

    suspend fun getActiveGateSync(): GateEntity? {
        return gateDao.getActiveGate()
    }

    suspend fun enterGate(gate: GateEntity) {
        // Mark active, set timestamps
        val now = System.currentTimeMillis()
        val end = now + gate.durationMillis
        val activeGate = gate.copy(
            isActive = true,
            startTimestamp = now,
            endTimestamp = end
        )
        gateDao.updateGate(activeGate)
    }

    suspend fun updateGate(gate: GateEntity) {
        gateDao.updateGate(gate)
    }

    suspend fun initializeGates() {
        val existing = gateDao.getActiveGate()
        if (existing == null) {
            val initialGates = listOf(
                GateEntity(
                    id = 1,
                    name = "E-Rank: Training Ground",
                    description = "Focus for 10 minutes.",
                    durationDays = 0,
                    durationMillis = 10 * 60 * 1000L,
                    requiredLevel = 1,
                    xpReward = 500
                ),
                GateEntity(
                    id = 2,
                    name = "D-Rank: Goblin Hideout",
                    description = "Focus for 30 minutes.",
                    durationDays = 0,
                    durationMillis = 30 * 60 * 1000L,
                    requiredLevel = 5,
                    xpReward = 2000
                )
            )
            gateDao.insertGates(initialGates)
        }
    }

    suspend fun unlockGatesForRank(rank: String) {
        val newGates = when (rank) {
            "D" -> listOf(
                GateEntity(id=3, name="D-Rank: Orc Outpost", description="Focus for 45 minutes.", durationDays=0, durationMillis=45 * 60 * 1000L, requiredLevel=5, xpReward=3500)
            )
            "C" -> listOf(
                GateEntity(id=4, name="C-Rank: Frost Dungeon", description="Focus for 60 minutes.", durationDays=0, durationMillis=60 * 60 * 1000L, requiredLevel=15, xpReward=8000)
            )
            "B" -> listOf(
                GateEntity(id=5, name="B-Rank: Demon Citadel", description="Focus for 90 minutes.", durationDays=0, durationMillis=90 * 60 * 1000L, requiredLevel=25, xpReward=15000)
            )
            "A" -> listOf(
                GateEntity(id=6, name="A-Rank: Ant Queen Nest", description="Focus for 120 minutes.", durationDays=0, durationMillis=120 * 60 * 1000L, requiredLevel=40, xpReward=35000)
            )
            "S" -> listOf(
                GateEntity(id=7, name="S-Rank: Jeju Island Raid", description="Focus for 240 minutes.", durationDays=0, durationMillis=240 * 60 * 1000L, requiredLevel=60, xpReward=100000)
            )
            else -> emptyList()
        }
        gateDao.insertGates(newGates)
    }

    suspend fun insertGates(gates: List<GateEntity>) {
        gateDao.insertGates(gates)
    }
}
