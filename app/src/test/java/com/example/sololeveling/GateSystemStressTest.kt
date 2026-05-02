package com.example.sololeveling

import com.example.sololeveling.data.entity.GateEntity
import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.data.repository.GateRepository
import com.example.sololeveling.data.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.example.sololeveling.data.dao.GateDao
import com.example.sololeveling.data.dao.UserDao

class GateSystemStressTest {

    private lateinit var gateRepository: GateRepository
    private lateinit var userRepository: UserRepository
    private val gateDao = mockk<GateDao>(relaxed = true)
    private val userDao = mockk<UserDao>(relaxed = true)

    @Before
    fun setup() {
        every { gateDao.getAllGates() } returns flowOf(emptyList())
        every { gateDao.getActiveGateFlow() } returns flowOf(null)
        every { userDao.getUser() } returns flowOf(null)
        
        gateRepository = GateRepository(gateDao)
        userRepository = UserRepository(userDao)
    }

    @Test
    fun `test gate entry persistence`() = runBlocking {
        val gate = GateEntity(id = 1, name = "Test Gate", description = "Test", durationDays = 0, durationMillis = 60000, requiredLevel = 1, xpReward = 100)
        
        coEvery { gateDao.updateGate(any()) } just Runs
        
        gateRepository.enterGate(gate)
        
        coVerify { gateDao.updateGate(match { 
            it.isActive && it.endTimestamp > it.startTimestamp
        }) }
    }

    @Test
    fun `test red gate upgrade probability`() {
        var redCount = 0
        val trials = 1000
        for (i in 1..trials) {
            val isRed = if (kotlin.random.Random.nextFloat() < 0.15f) true else false
            if (isRed) redCount++
        }
        
        // Probability should be around 15% (150/1000)
        assertTrue("Red gate count was $redCount", redCount in 100..200)
    }

    @Test
    fun `test gate failure penalty`() = runBlocking {
        val user = UserEntity(id = 1, username = "Jinwoo", endurance = 100, maxEndurance = 100)
        coEvery { userDao.getUserSync() } returns user
        
        // Simulate failure penalty logic
        val isRedGate = true
        val enduranceLoss = if (isRedGate) 30 else 10
        userRepository.decreaseEndurance(user, enduranceLoss)
        
        coVerify { userDao.updateUser(match { it.endurance == 70 }) }
    }

    @Test
    fun `test massive xp reward on gate clear`() = runBlocking {
        val user = UserEntity(id = 1, username = "Jinwoo", level = 1, currentXP = 0)
        coEvery { userDao.getUserSync() } returns user
        
        val xpReward = 10000 // Massive reward
        userRepository.grantGateRewards(xpReward)
        
        coVerify { userDao.updateUser(match { it.level > 1 && it.hasClearedGateSincePromotion }) }
    }
    
    @Test
    fun `test gate cooldown block logic`() = runBlocking {
        val now = System.currentTimeMillis()
        val gate = GateEntity(
            id = 1, 
            name = "Test Gate", 
            description = "Test", 
            durationDays = 0,
            cooldownEnd = now + 100000, 
            requiredLevel = 1, 
            xpReward = 100
        )
        
        val canEnter = now >= gate.cooldownEnd
        assertFalse(canEnter)
    }
}
