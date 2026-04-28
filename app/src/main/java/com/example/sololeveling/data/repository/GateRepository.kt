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
        val end = now + (gate.durationDays * 24 * 60 * 60 * 1000L)
        val activeGate = gate.copy(
            isActive = true,
            startTimestamp = now,
            endTimestamp = end,
            daysCompleted = 0
        )
        gateDao.updateGate(activeGate)
    }

    suspend fun updateGate(gate: GateEntity) {
        gateDao.updateGate(gate)
    }

    suspend fun initializeGates() {
        // Pre-populate some gates if empty (check omitted for brevity, assuming run once or insert conflicts replace)
        // In real app, check ID existence.
        // ID 1: E-Rank Gate (7 Days)
        val gates = listOf(
            GateEntity(
                id = 1,
                name = "E-Rank Dungeon: Goblin Cave",
                description = "Maintain a perfect streak for 7 days.",
                durationDays = 7,
                requiredLevel = 1,
                xpReward = 1000
            ),
            GateEntity(
                id = 2,
                name = "D-Rank Dungeon: Lizardmen Swamp",
                description = "Maintain a perfect streak for 14 days.",
                durationDays = 14,
                requiredLevel = 5,
                xpReward = 3000
            )
        )
        gateDao.insertGates(gates)
    }
    suspend fun unlockGatesForRank(rank: String) {
        val newGates = when (rank) {
            "D" -> listOf(
                GateEntity(id=3, name="D-Rank: Stone Golem Pit", description="Maintain a perfect streak for 10 days.", durationDays=10, requiredLevel=5, xpReward=2000)
            )
            "C" -> listOf(
                GateEntity(id=4, name="C-Rank: Ice Elf Forest", description="Maintain a perfect streak for 14 days.", durationDays=14, requiredLevel=15, xpReward=5000)
            )
            "B" -> listOf(
                GateEntity(id=5, name="B-Rank: Demon Castle", description="Maintain a perfect streak for 21 days.", durationDays=21, requiredLevel=25, xpReward=10000)
            )
            "A" -> listOf(
                GateEntity(id=6, name="A-Rank: Ant Tunnel", description="Maintain a perfect streak for 30 days.", durationDays=30, requiredLevel=40, xpReward=20000)
            )
            "S" -> listOf(
                GateEntity(id=7, name="S-Rank: Jeju Island", description="Maintain a perfect streak for 50 days.", durationDays=50, requiredLevel=60, xpReward=50000)
            )
            else -> emptyList()
        }
        gateDao.insertGates(newGates)
    }

    suspend fun insertGates(gates: List<GateEntity>) {
        gateDao.insertGates(gates)
    }
}
