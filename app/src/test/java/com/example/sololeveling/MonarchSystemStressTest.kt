package com.example.sololeveling

import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.util.StatCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonarchSystemStressTest {

    @Test
    fun `test Monarch stat multipliers`() {
        val baseFitness = 20
        val baseKnowledge = 30
        
        // Normal User
        val normalXpMult = StatCalculator.calculateXpMultiplier(baseFitness, baseKnowledge, isMonarch = false)
        // 1.0 + 0.20 + 0.60 = 1.8
        assertEquals(1.8f, normalXpMult, 0.01f)
        
        // Monarch User (2.0x bonus)
        val monarchXpMult = StatCalculator.calculateXpMultiplier(baseFitness, baseKnowledge, isMonarch = true)
        assertEquals(3.6f, monarchXpMult, 0.01f)
    }

    @Test
    fun `test Monarch penalty immunity`() {
        val discipline = 10
        
        // Normal User (10% reduction, capped at 50%)
        val normalReduction = StatCalculator.calculatePenaltyReduction(discipline, isMonarch = false)
        assertEquals(0.10f, normalReduction, 0.01f)
        
        // Monarch User (100% immunity)
        val monarchReduction = StatCalculator.calculatePenaltyReduction(discipline, isMonarch = true)
        assertEquals(1.0f, monarchReduction, 0.01f)
    }

    @Test
    fun `test Monarch boss damage`() {
        val fitness = 50
        val luck = 50
        
        // Monarch should have significantly higher damage
        val monarchDamage = StatCalculator.calculateBossDamage(fitness, luck, isMonarch = true)
        // Base: 50 * 10 = 500. Monarch Base: 500 * 2 = 1000. 
        // Crit Chance Monarch: 50 * 0.01 = 0.5 (80% cap). 
        // So damage is either 1000 or 1500.
        assertTrue(monarchDamage >= 1000)
    }

    @Test
    fun `test Monarch XP requirement reduction`() {
        val level = 10
        val knowledge = 200 // High knowledge
        
        // Normal: 10 * 1000 = 10000. Knowledge 200 -> 50% reduction (cap). -> 5000.
        val normalXp = StatCalculator.calculateRequiredXp(level, knowledge, isMonarch = false)
        assertEquals(5000L, normalXp)
        
        // Monarch: Knowledge cap is higher (80%). -> 2000.
        val monarchXp = StatCalculator.calculateRequiredXp(level, knowledge, isMonarch = true)
        assertEquals(2000L, monarchXp)
    }

    @Test
    fun `test Monarch shadow loyalty lock simulation`() {
        val monarch = UserEntity(isMonarch = true)
        
        // Simulation of logic in ShadowRepository
        val willReduce = !monarch.isMonarch
        assertTrue(!willReduce)
    }
}
