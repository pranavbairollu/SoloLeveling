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
    
    suspend fun decreaseEndurance(user: UserEntity): Boolean {
        // Return true if System Reset triggered
        val newEndurance = (user.endurance - 1).coerceAtLeast(0)
        
        if (newEndurance <= 0) {
            // Trigger Reset Logic should be handled by caller (ViewModel) to coordinate Shadows etc.
            // Just update value here? No, caller needs to know to trigger reset.
            updateUser(user.copy(endurance = 0))
            return true
        } else {
            updateUser(user.copy(endurance = newEndurance))
            return false
        }
    }
    
    suspend fun processDailyEnduranceGain(user: UserEntity) {
        if (user.endurance < user.maxEndurance) {
            updateUser(user.copy(endurance = user.endurance + 1))
        }
    }
    
    suspend fun performSystemReset(user: UserEntity) {
        // Level -1 (Min 1)
        val newLevel = (user.level - 1).coerceAtLeast(1)
        val newXp = 0L
        
        // Stats -10%
        val newStr = (user.fitness * 0.9).toInt().coerceAtLeast(1)
        val newInt = (user.knowledge * 0.9).toInt().coerceAtLeast(1)
        val newDisc = (user.discipline * 0.9).toInt().coerceAtLeast(1)
        val newAwa = (user.awareness * 0.9).toInt().coerceAtLeast(1)
        
        // Endurance Reset
        val newEndurance = user.maxEndurance
        
        val resetUser = user.copy(
            level = newLevel,
            currentXP = newXp,
            fitness = newStr,
            knowledge = newInt,
            discipline = newDisc,
            awareness = newAwa,
            endurance = newEndurance,
            penaltyEndTime = 0L // Clear penalty logic if reset? Or keep it? Requirement doesn't strict penalty clear but reset implies fresh start partially. "Endurance reset to max".
            // "Active Gate failed" -> Handled by GateRepo
            // "Shadow loyalty reduced" -> Handled by ShadowRepo
        )
        updateUser(resetUser)
    }
}
