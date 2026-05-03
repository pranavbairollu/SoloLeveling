package com.example.sololeveling

import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.data.repository.UserRepository
import com.example.sololeveling.data.dao.UserDao
import com.example.sololeveling.data.entity.QuestEntity
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SystemUXStressTest {

    private val userDao = mockk<UserDao>(relaxed = true)
    private val userRepository = UserRepository(userDao)

    @Test
    fun `test system reset logic robustness`() = runTest {
        val user = UserEntity(
            level = 10, 
            fitness = 50, 
            knowledge = 50,
            currentXP = 500,
            unspentPoints = 15
        )
        coEvery { userDao.updateUser(any()) } just Runs
        
        userRepository.performSystemReset(user)
        
        val captor = slot<UserEntity>()
        coVerify { userDao.updateUser(capture(captor)) }
        
        val result = captor.captured
        assertEquals("Level should be reduced", 9, result.level)
        assertEquals("Fitness should be reduced by 10%", 45, result.fitness)
        assertEquals("Knowledge should be reduced by 10%", 45, result.knowledge)
        assertEquals("XP should be wiped", 0L, result.currentXP)
        assertEquals("Unspent points should be wiped", 0, result.unspentPoints)
        assertEquals("Endurance should be restored to max", result.maxEndurance, result.endurance)
    }

    @Test
    fun `verify monarch immunity to reset`() = runTest {
        val monarch = UserEntity(level = 100, isMonarch = true)
        
        userRepository.performSystemReset(monarch)
        
        // No update should happen for Monarch
        coVerify(exactly = 0) { userDao.updateUser(any()) }
    }

    @Test
    fun `test massive xp gain and level up rollover`() = runTest {
        val user = UserEntity(level = 1, currentXP = 0, knowledge = 10)
        coEvery { userDao.getUserSync() } returns user
        coEvery { userDao.updateUser(any()) } just Runs
        
        // Grant 10,000 XP (Level 1 req is 1000)
        userRepository.grantGateRewards(10000)
        
        val captor = slot<UserEntity>()
        coVerify { userDao.updateUser(capture(captor)) }
        
        val result = captor.captured
        assertTrue("Level should have increased", result.level > 3)
        assertTrue("Unspent points should be awarded", result.unspentPoints > 0)
    }
}
