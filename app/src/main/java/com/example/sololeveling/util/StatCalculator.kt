package com.example.sololeveling.util

object StatCalculator {

    // FITNESS (STR): Increases Quest XP by +1% per point
    // KNOWLEDGE (INT): Increases Quest XP by +2% per point
    fun calculateXpMultiplier(fitness: Int, knowledge: Int): Float {
        val strBonus = fitness * 0.01f
        val intBonus = knowledge * 0.02f
        return 1.0f + strBonus + intBonus
    }

    // BOSS DAMAGE
    fun calculateBossDamage(fitness: Int, luck: Int): Int {
        val base = fitness * 10
        val critChance = calculateCritChance(luck)
        return if (Math.random() < critChance) (base * 1.5).toInt() else base
    }

    // DISCIPLINE (VIT): Max HP +5 per point
    fun calculateMaxHp(discipline: Int): Int {
        return 100 + (discipline * 5)
    }

    // DISCIPLINE: Penalty Severity Reduction (Cap 50%)
    fun calculatePenaltyReduction(discipline: Int): Float {
        val reduction = discipline * 0.01f
        return reduction.coerceAtMost(0.50f)
    }

    // KNOWLEDGE: Level XP Requirement Reduction
    fun calculateRequiredXp(level: Int, knowledge: Int): Long {
        val baseXp = level * 1000L // Increased base for longer progression
        val reductionFactor = (knowledge * 0.005f).coerceAtMost(0.5f)
        val reduced = baseXp * (1.0f - reductionFactor)
        return reduced.toLong().coerceAtLeast(100L) 
    }

    // AWARENESS (AGI): Reward Chance Bonus
    fun calculateBonusRewardChance(awareness: Int, luck: Int): Float {
        // 0.5% from Awareness + 1% from Luck
        return (awareness * 0.005f) + (luck * 0.01f)
    }

    // LUCK: Crit Chance for Bosses (Cap 40%)
    fun calculateCritChance(luck: Int): Float {
        return (luck * 0.01f).coerceAtMost(0.40f)
    }

    // CHARISMA: Shadow Recruitment/Loyalty Bonus
    fun calculateCharismaBonus(charisma: Int): Float {
        return (charisma * 0.02f).coerceAtMost(0.60f)
    }
}
