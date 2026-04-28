package com.example.sololeveling.logic

import com.example.sololeveling.data.entity.*

enum class SupportedClass {
    STUDENT, ATHLETE, EXECUTIVE, CUSTOM
}

object ClassManager {

    fun getDefaultQuestsForClass(classType: SupportedClass): List<QuestEntity> {
        val today = System.currentTimeMillis()
        // Note: Dates might need adjustment in ViewModel, this provides template.
        
        return when (classType) {
            SupportedClass.STUDENT -> listOf(
                QuestEntity(title = "Study Session", linkedStat = "Knowledge", requirementTarget = 60, requirementUnit = "Minutes", difficulty = "C", date = today, generatedByClass = "Student"),
                QuestEntity(title = "Daily Exercise", linkedStat = "Fitness", requirementTarget = 30, requirementUnit = "Reps", difficulty = "D", date = today, generatedByClass = "Student"),
                QuestEntity(title = "Reading", linkedStat = "Knowledge", requirementTarget = 20, requirementUnit = "Pages", difficulty = "E", date = today, generatedByClass = "Student")
            )
            SupportedClass.ATHLETE -> listOf(
                QuestEntity(title = "Morning Run", linkedStat = "Fitness", requirementTarget = 30, requirementUnit = "Minutes", difficulty = "C", date = today, generatedByClass = "Athlete"),
                QuestEntity(title = "Strength Training", linkedStat = "Fitness", requirementTarget = 100, requirementUnit = "Reps", difficulty = "C", date = today, generatedByClass = "Athlete"),
                QuestEntity(title = "Focus Meditation", linkedStat = "Discipline", requirementTarget = 15, requirementUnit = "Minutes", difficulty = "D", date = today, generatedByClass = "Athlete")
            )
            SupportedClass.EXECUTIVE -> listOf(
                QuestEntity(title = "Strategic Reading", linkedStat = "Knowledge", requirementTarget = 30, requirementUnit = "Pages", difficulty = "D", date = today, generatedByClass = "Executive"),
                QuestEntity(title = "Networking", linkedStat = "Charisma", requirementTarget = 1, requirementUnit = "Event", difficulty = "B", date = today, generatedByClass = "Executive"),
                QuestEntity(title = "Deep Work", linkedStat = "Discipline", requirementTarget = 90, requirementUnit = "Minutes", difficulty = "C", date = today, generatedByClass = "Executive")
            )
            SupportedClass.CUSTOM -> emptyList()
        }
    }

    fun getDefaultGatesForClass(classType: SupportedClass): List<GateEntity> {
        return when (classType) {
            SupportedClass.STUDENT -> listOf(
                GateEntity(id = 1, name = "Exam Week", description = "Maintain focus for 7 days.", durationDays = 7, requiredLevel = 1, xpReward = 1000, failCondition = "Score < 80%")
            )
            SupportedClass.ATHLETE -> listOf(
                GateEntity(id = 1, name = "Marathon Prep", description = "Train daily for 14 days.", durationDays = 14, requiredLevel = 1, xpReward = 2000, failCondition = "Miss > 2 Days")
            )
            SupportedClass.EXECUTIVE -> listOf(
                GateEntity(id = 1, name = "Project Launch", description = "Execute launch plan over 30 days.", durationDays = 30, requiredLevel = 1, xpReward = 5000, failCondition = "Miss Deadlines")
            )
            SupportedClass.CUSTOM -> emptyList()
        }
    }

    fun getDefaultBossesForClass(classType: SupportedClass): List<BossEntity> {
        return when (classType) {
            SupportedClass.STUDENT -> listOf(
                BossEntity(id = 1, name = "Finals Week", description = "The ultimate test of knowledge.", rank = "C", requiredLevel = 5, requiredFitness = 10, requiredKnowledge = 50, requiredDiscipline = 30, xpReward = 5000)
            )
            SupportedClass.ATHLETE -> listOf(
                BossEntity(id = 1, name = "The Championship", description = "Peak physical performance required.", rank = "B", requiredLevel = 10, requiredFitness = 60, requiredKnowledge = 10, requiredDiscipline = 40, xpReward = 8000)
            )
            SupportedClass.EXECUTIVE -> listOf(
                BossEntity(id = 1, name = "Board Meeting", description = "High stakes presentation.", rank = "A", requiredLevel = 15, requiredFitness = 20, requiredKnowledge = 40, requiredCharisma = 50, requiredDiscipline = 30, xpReward = 10000)
            )
            SupportedClass.CUSTOM -> emptyList()
        }
    }

    fun getDefaultShadowsForClass(classType: SupportedClass): List<ShadowEntity> {
         return when (classType) {
            SupportedClass.STUDENT -> listOf(
                ShadowEntity(name = "Mentor: Professor", sourceBossId = 1, boostedStat = "Knowledge", boostMultiplier = 1.1, unlockCondition = "Defeat Finals Week")
            )
            SupportedClass.ATHLETE -> listOf(
                ShadowEntity(name = "Mentor: Coach", sourceBossId = 1, boostedStat = "Fitness", boostMultiplier = 1.2, unlockCondition = "Defeat Championship")
            )
            SupportedClass.EXECUTIVE -> listOf(
                ShadowEntity(name = "Mentor: Chairman", sourceBossId = 1, boostedStat = "Charisma", boostMultiplier = 1.3, unlockCondition = "Defeat Board Meeting")
            )
            SupportedClass.CUSTOM -> emptyList()
        }
    }

    fun getDefaultMonarchForClass(classType: SupportedClass): MonarchEntity {
        return when (classType) {
            SupportedClass.STUDENT -> MonarchEntity(title = "Valedictorian", victoryCondition = "Ph.D or Equivalent Mastery", requiredAggregateStats = 500)
            SupportedClass.ATHLETE -> MonarchEntity(title = "Olympian", victoryCondition = "Gold Medal / World Record", requiredAggregateStats = 600)
            SupportedClass.EXECUTIVE -> MonarchEntity(title = "Tycoon", victoryCondition = "Net Worth > $10M", requiredAggregateStats = 550)
            SupportedClass.CUSTOM -> MonarchEntity(title = "Monarch", victoryCondition = "Complete The System", requiredAggregateStats = 1000)
        }
    }

    fun getClassStatFocus(classType: SupportedClass): Map<String, String> {
        return when (classType) {
            SupportedClass.STUDENT -> mapOf("Primary" to "Knowledge", "Secondary" to "Discipline")
            SupportedClass.ATHLETE -> mapOf("Primary" to "Fitness", "Secondary" to "Discipline")
            SupportedClass.EXECUTIVE -> mapOf("Primary" to "Charisma", "Secondary" to "Awareness")
            SupportedClass.CUSTOM -> emptyMap()
        }
    }
}
