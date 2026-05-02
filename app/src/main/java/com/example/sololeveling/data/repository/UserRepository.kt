package com.example.sololeveling.data.repository

import com.example.sololeveling.data.dao.UserDao
import com.example.sololeveling.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    val userFlow: Flow<UserEntity?> = userDao.getUser()

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
    
    suspend fun getCurrentUser(): UserEntity? {
        return userDao.getUserSync()
    }
    
    suspend fun decreaseEndurance(user: UserEntity, amount: Int = 1): Boolean {
        // Return true if System Reset triggered
        val newEndurance = (user.endurance - amount).coerceAtLeast(0)
        
        updateUser(user.copy(endurance = newEndurance))
        
        return if (newEndurance <= 0) {
            // Survival Failure Triggered
            true
        } else {
            false
        }
    }
    
    suspend fun processDailyEnduranceGain(user: UserEntity) {
        if (user.endurance < user.maxEndurance) {
            val gain = 5 // Bonus for completing all quests
            val newEndurance = (user.endurance + gain).coerceAtMost(user.maxEndurance)
            updateUser(user.copy(endurance = newEndurance))
        }
    }
    
    suspend fun performSystemReset(user: UserEntity) {
        // Level -1 (Min 1)
        val newLevel = (user.level - 1).coerceAtLeast(1)
        val newXp = 0L
        
        // Stats -10% (Survival Penalty)
        val newStr = (user.fitness * 0.9).toInt().coerceAtLeast(10)
        val newInt = (user.knowledge * 0.9).toInt().coerceAtLeast(10)
        val newDisc = (user.discipline * 0.9).toInt().coerceAtLeast(10)
        val newAwa = (user.awareness * 0.9).toInt().coerceAtLeast(10)
        val newChr = (user.charisma * 0.9).toInt().coerceAtLeast(10)
        val newLuk = (user.luck * 0.9).toInt().coerceAtLeast(10)
        
        // Recalculate Max HP
        val newMaxHp = com.example.sololeveling.util.StatCalculator.calculateMaxHp(newDisc)
        
        val resetUser = user.copy(
            level = newLevel,
            currentXP = newXp,
            fitness = newStr,
            knowledge = newInt,
            discipline = newDisc,
            awareness = newAwa,
            charisma = newChr,
            luck = newLuk,
            maxEndurance = newMaxHp,
            endurance = newMaxHp, // Reset to max
            penaltyEndTime = 0L,
            unspentPoints = 0, // Reset unspent points as penalty
            hasClearedGateSincePromotion = false,
            hasDefeatedBossSincePromotion = false
        )
        updateUser(resetUser)
    }

    suspend fun grantGateRewards(xpReward: Int) {
        val user = getCurrentUser() ?: return
        
        // XP Calculation with Fitness/Knowledge Multiplier
        val xpMultiplier = com.example.sololeveling.util.StatCalculator.calculateXpMultiplier(user.fitness, user.knowledge)
        val bonusXp = (xpReward * xpMultiplier).toLong()
        
        var newXp = user.currentXP + bonusXp
        var newLevel = user.level
        var requiredXp = com.example.sololeveling.util.StatCalculator.calculateRequiredXp(newLevel, user.knowledge)
        var unspentPoints = user.unspentPoints
        
        // Level Up Check
        while (newXp >= requiredXp) {
            newXp -= requiredXp
            newLevel++
            requiredXp = com.example.sololeveling.util.StatCalculator.calculateRequiredXp(newLevel, user.knowledge)
            unspentPoints += 3
        }

        updateUser(user.copy(
            level = newLevel,
            currentXP = newXp,
            unspentPoints = unspentPoints,
            hasClearedGateSincePromotion = true
        ))
    }
}
