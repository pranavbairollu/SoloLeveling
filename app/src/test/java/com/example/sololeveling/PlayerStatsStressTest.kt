package com.example.sololeveling

import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.data.repository.UserRepository
import com.example.sololeveling.ui.dashboard.MainViewModel
import com.example.sololeveling.data.dao.UserDao
import com.example.sololeveling.data.entity.QuestEntity
import com.example.sololeveling.data.repository.BossRepository
import com.example.sololeveling.data.repository.GateRepository
import com.example.sololeveling.data.repository.QuestRepository
import com.example.sololeveling.data.repository.ShadowRepository
import com.example.sololeveling.logic.StatType
import com.example.sololeveling.util.StatCalculator
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerStatsStressTest {

    @get:org.junit.Rule
    val instantTaskExecutorRule = androidx.arch.core.executor.testing.InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var questRepository: QuestRepository
    private lateinit var gateRepository: GateRepository
    private lateinit var bossRepository: BossRepository
    private lateinit var shadowRepository: ShadowRepository
    private lateinit var viewModel: MainViewModel
    
    private val userDao = mockk<UserDao>(relaxed = true)
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        userRepository = spyk(UserRepository(userDao))
        questRepository = mockk(relaxed = true)
        gateRepository = mockk(relaxed = true)
        bossRepository = mockk(relaxed = true)
        shadowRepository = mockk(relaxed = true)
        
        // Mock current user flow
        coEvery { userDao.getUserSync() } returns UserEntity()
        coEvery { userDao.getUser() } returns flowOf(UserEntity())
        
        viewModel = MainViewModel(userRepository, questRepository, gateRepository, bossRepository, shadowRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stress test level up and stat reward consistency`() = runTest {
        var currentUser = UserEntity(level = 1, currentXP = 0, unspentPoints = 0)
        coEvery { userRepository.getCurrentUser() } answers { currentUser }
        coEvery { userRepository.updateUser(any()) } answers { currentUser = firstArg() }

        val quest = QuestEntity(
            id = 1,
            title = "Stress Test Quest",
            xpReward = 1500, // Enough to level up
            statReward = 5,
            linkedStat = "FITNESS",
            date = System.currentTimeMillis()
        )

        // Simulate 100 quest completions
        repeat(100) {
            viewModel.completeQuest(quest)
            advanceUntilIdle()
        }

        // Verify state
        assertTrue("Level should have increased", currentUser.level > 1)
        assertTrue("Fitness should have increased by 500", currentUser.fitness >= 510) // 10 base + 500
        assertTrue("Unspent points should be awarded", currentUser.unspentPoints > 0)
        assertEquals("XP should not be negative", true, currentUser.currentXP >= 0)
    }

    @Test
    fun `stress test stat allocation safety`() = runTest {
        var currentUser = UserEntity(level = 50, unspentPoints = 1000)
        coEvery { userRepository.getCurrentUser() } answers { currentUser }
        coEvery { userRepository.updateUser(any()) } answers { currentUser = firstArg() }

        // Attempt to allocate points
        viewModel.allocatePoints(100, 100, 100, 100, 100, 100) // 600 total
        advanceUntilIdle()

        assertEquals("Fitness should be 110", 110, currentUser.fitness)
        assertEquals("Unspent points should be 400", 400, currentUser.unspentPoints)
        
        // Attempt to OVERSPEND
        viewModel.allocatePoints(500, 0, 0, 0, 0, 0)
        advanceUntilIdle()
        
        assertEquals("Should not have allocated points due to insufficient balance", 400, currentUser.unspentPoints)
        assertEquals("Fitness should remain 110", 110, currentUser.fitness)
    }

    @Test
    fun `stress test survival failure and system reset`() = runTest {
        var currentUser = UserEntity(
            level = 10,
            fitness = 50,
            knowledge = 50,
            discipline = 50,
            endurance = 20,
            maxEndurance = 350 // (50 * 5) + 100
        )
        coEvery { userRepository.getCurrentUser() } answers { currentUser }
        coEvery { userRepository.updateUser(any()) } answers { currentUser = firstArg() }

        // Simulate massive damage
        userRepository.decreaseEndurance(currentUser, 50)
        advanceUntilIdle()
        
        // The ViewModel triggers performSystemReset when decreaseEndurance returns true
        val resetTriggered = userRepository.decreaseEndurance(currentUser, 50)
        if (resetTriggered) {
             userRepository.performSystemReset(currentUser)
        }
        advanceUntilIdle()

        assertEquals("Level should have decreased by 1", 9, currentUser.level)
        assertEquals("Endurance should be reset to max", currentUser.maxEndurance, currentUser.endurance)
        assertTrue("Stats should be reduced by 10%", currentUser.fitness < 50)
        assertEquals("XP should be reset to 0", 0L, currentUser.currentXP)
        assertEquals("Unspent points should be reset to 0", 0, currentUser.unspentPoints)
    }

    @Test
    fun `stress test rank promotion sequence`() = runTest {
        var currentUser = UserEntity(rank = "E", level = 1)
        coEvery { userRepository.getCurrentUser() } answers { currentUser }
        coEvery { userRepository.updateUser(any()) } answers { currentUser = firstArg() }

        // Try to promote without criteria
        viewModel.promoteUser()
        advanceUntilIdle()
        assertEquals("Should still be E-Rank", "E", currentUser.rank)

        // Satisfy criteria for D-Rank
        currentUser = currentUser.copy(level = 5, hasClearedGateSincePromotion = true, hasDefeatedBossSincePromotion = true)
        viewModel.promoteUser()
        advanceUntilIdle()
        assertEquals("Should be D-Rank", "D", currentUser.rank)
        
        // Loop through all ranks
        val ranks = listOf("C", "B", "A", "S")
        ranks.forEach { targetRank ->
            currentUser = currentUser.copy(
                level = 100, 
                hasClearedGateSincePromotion = true, 
                hasDefeatedBossSincePromotion = true
            )
            viewModel.promoteUser()
            advanceUntilIdle()
        }
        
        assertEquals("Should be S-Rank", "S", currentUser.rank)
    }
}
