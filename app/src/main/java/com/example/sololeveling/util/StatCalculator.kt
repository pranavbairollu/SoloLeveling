package com.example.sololeveling.util

object StatCalculator {

    // STR: Increases Quest XP by +1% per point
    fun calculateXpMultiplier(strength: Int, intelligence: Int): Float {
        val strBonus = strength * 0.01f
        val intBonus = intelligence * 0.02f
        // Base 100% + Bonuses
        return 1.0f + strBonus + intBonus
    }

    // BOSS DAMAGE (Future Hook)
    fun calculateBossDamage(strength: Int): Int {
        return strength * 10 // Placeholder formula
    }

    // VIT (Discipline): Max HP +5 per point
    // Note: If this is "Lives" (Endurance), +5 per point is huge. 
    // If this is Combat HP, it's fine. 
    // Assuming this scales the 'maxEndurance' property.
    fun calculateMaxHp(vitality: Int): Int {
        // Base 3 + (VIT * 5)? Or just VIT * 5?
        // Prompt says "Increases Max HP by +5 per point".
        // Assuming base is 0 or 100?
        // Let's assume it adds to the base durability of the player.
        // Existing UserEntity has default 3.
        // If we want to keep it "Lives" style, maybe +1 per 10 points?
        // But prompt is specific: "+5 per point".
        // I will follow prompt. Maybe usage changes from "Lives" to "Health".
        // Base 100 + (VIT * 5)
        return 100 + (vitality * 5)
    }

    // VIT: Penalty Severity Reduction (Cap 30%)
    fun calculatePenaltyReduction(vitality: Int): Float {
        // 1% per point
        val reduction = vitality * 0.01f
        return reduction.coerceAtMost(0.30f)
    }

    // INT: Level XP Requirement Reduction
    // "Slightly reduces Level XP (soft curve)"
    fun calculateRequiredXp(level: Int, intelligence: Int): Long {
        val baseXp = level * 100L
        // Soft curve reduction: 0.1% per INT point?
        // Let's say max reduction 50%.
        val reductionFactor = (intelligence * 0.005f).coerceAtMost(0.5f)
        val reduced = baseXp * (1.0f - reductionFactor)
        return reduced.toLong().coerceAtLeast(10L) 
    }

    // AGI (Awareness): Reward Chance Bonus
    fun calculateBonusRewardChance(agility: Int): Float {
        // 0.5% per point
        return agility * 0.005f
    }
}
