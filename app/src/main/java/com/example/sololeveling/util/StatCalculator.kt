package com.example.sololeveling.util

object StatCalculator {

    // FITNESS (STR): Increases Quest XP by +1% per point
    fun calculateXpMultiplier(fitness: Int, knowledge: Int, shadowMult: Float = 1.0f): Float {
        val strBonus = fitness * 0.01f
        val intBonus = knowledge * 0.02f
        return (1.0f + strBonus + intBonus) * shadowMult
    }

    // BOSS DAMAGE
    fun calculateBossDamage(fitness: Int, luck: Int, shadowMult: Float = 1.0f): Int {
        val base = (fitness * 10 * shadowMult).toInt()
        val critChance = calculateCritChance(luck)
        return if (Math.random() < critChance) (base * 1.5).toInt() else base
    }

    // DISCIPLINE (VIT): Max HP +5 per point
    fun calculateMaxHp(discipline: Int, shadowMult: Float = 1.0f): Int {
        val effectiveDisc = (discipline * shadowMult).toInt()
        return 100 + (effectiveDisc * 5)
    }

    // DISCIPLINE: Penalty Severity Reduction (Cap 50%)
    fun calculatePenaltyReduction(discipline: Int, shadowMult: Float = 1.0f): Float {
        val effectiveDisc = (discipline * shadowMult).toInt()
        val reduction = effectiveDisc * 0.01f
        return reduction.coerceAtMost(0.50f)
    }

    // KNOWLEDGE: Level XP Requirement Reduction
    fun calculateRequiredXp(level: Int, knowledge: Int, shadowMult: Float = 1.0f): Long {
        val baseXp = level * 1000L
        val effectiveKnowledge = (knowledge * shadowMult).toInt()
        val reductionFactor = (effectiveKnowledge * 0.005f).coerceAtMost(0.5f)
        val reduced = baseXp * (1.0f - reductionFactor)
        return reduced.toLong().coerceAtLeast(100L) 
    }

    // AWARENESS (AGI): Reward Chance Bonus
    fun calculateBonusRewardChance(awareness: Int, luck: Int, shadowMult: Float = 1.0f): Float {
        val effectiveAwa = (awareness * shadowMult).toInt()
        return (effectiveAwa * 0.005f) + (luck * 0.01f)
    }

    // LUCK: Crit Chance for Bosses (Cap 40%)
    fun calculateCritChance(luck: Int, shadowMult: Float = 1.0f): Float {
        val effectiveLuck = (luck * shadowMult).toInt()
        return (effectiveLuck * 0.01f).coerceAtMost(0.40f)
    }

    // CHARISMA: Shadow Recruitment/Loyalty Bonus
    fun calculateCharismaBonus(charisma: Int, shadowMult: Float = 1.0f): Float {
        val effectiveChr = (charisma * shadowMult).toInt()
        return (effectiveChr * 0.02f).coerceAtMost(0.60f)
    }
}
