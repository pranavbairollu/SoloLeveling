package com.example.sololeveling.util

object StatCalculator {

    const val MONARCH_MULTIPLIER = 2.0f

    private fun applyMonarchBonus(value: Float, isMonarch: Boolean): Float {
        return if (isMonarch) value * MONARCH_MULTIPLIER else value
    }

    private fun applyMonarchBonus(value: Long, isMonarch: Boolean): Long {
        return if (isMonarch) (value * MONARCH_MULTIPLIER).toLong() else value
    }

    private fun applyMonarchBonus(value: Int, isMonarch: Boolean): Int {
        return if (isMonarch) (value * MONARCH_MULTIPLIER).toInt() else value
    }

    // FITNESS (STR): Increases Quest XP by +1% per point
    fun calculateXpMultiplier(fitness: Int, knowledge: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Float {
        val strBonus = fitness * 0.01f
        val intBonus = knowledge * 0.02f
        val base = (1.0f + strBonus + intBonus) * shadowMult
        return applyMonarchBonus(base, isMonarch)
    }

    // BOSS DAMAGE
    fun calculateBossDamage(fitness: Int, luck: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Int {
        val base = (fitness * 10 * shadowMult).toInt()
        val critChance = calculateCritChance(luck, shadowMult, isMonarch)
        val finalBase = if (Math.random() < critChance) (base * 1.5).toInt() else base
        return applyMonarchBonus(finalBase, isMonarch)
    }

    // DISCIPLINE (VIT): Max HP +5 per point
    fun calculateMaxHp(discipline: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Int {
        val effectiveDisc = (discipline * shadowMult).toInt()
        val baseMaxHp = 100 + (effectiveDisc * 5)
        return applyMonarchBonus(baseMaxHp, isMonarch)
    }

    // DISCIPLINE: Penalty Severity Reduction (Cap 50%, or 100% for Monarch)
    fun calculatePenaltyReduction(discipline: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Float {
        if (isMonarch) return 1.0f // 100% reduction
        val effectiveDisc = (discipline * shadowMult).toInt()
        val reduction = effectiveDisc * 0.01f
        return reduction.coerceAtMost(0.50f)
    }

    // KNOWLEDGE: Level XP Requirement Reduction
    fun calculateRequiredXp(level: Int, knowledge: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Long {
        val baseXp = level * 1000L
        val effectiveKnowledge = (knowledge * shadowMult).toInt()
        val reductionFactor = (effectiveKnowledge * 0.005f).coerceAtMost(if (isMonarch) 0.8f else 0.5f)
        val reduced = baseXp * (1.0f - reductionFactor)
        return kotlin.math.round(reduced).toLong().coerceAtLeast(100L) 
    }

    // AWARENESS (AGI): Reward Chance Bonus
    fun calculateBonusRewardChance(awareness: Int, luck: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Float {
        val effectiveAwa = (awareness * shadowMult).toInt()
        val baseChance = (effectiveAwa * 0.005f) + (luck * 0.01f)
        return applyMonarchBonus(baseChance, isMonarch)
    }

    // LUCK: Crit Chance for Bosses (Cap 40%, or 80% for Monarch)
    fun calculateCritChance(luck: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Float {
        val effectiveLuck = (luck * shadowMult).toInt()
        val chance = (effectiveLuck * 0.01f).coerceAtMost(if (isMonarch) 0.80f else 0.40f)
        return chance
    }

    // CHARISMA: Shadow Recruitment/Loyalty Bonus
    fun calculateCharismaBonus(charisma: Int, shadowMult: Float = 1.0f, isMonarch: Boolean = false): Float {
        if (isMonarch) return 1.0f // Max bonus
        val effectiveChr = (charisma * shadowMult).toInt()
        return (effectiveChr * 0.02f).coerceAtMost(0.60f)
    }
}
