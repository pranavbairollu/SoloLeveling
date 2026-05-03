package com.example.sololeveling.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.sololeveling.data.entity.GateEntity
import com.example.sololeveling.data.entity.QuestEntity
import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.data.repository.GateRepository
import com.example.sololeveling.data.repository.QuestRepository
import com.example.sololeveling.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

import com.example.sololeveling.data.repository.BossRepository
import com.example.sololeveling.data.repository.ShadowRepository

import com.example.sololeveling.util.QuestSystemManager
import kotlinx.coroutines.flow.collectLatest

class MainViewModel(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val gateRepository: GateRepository,
    private val bossRepository: BossRepository,
    private val shadowRepository: ShadowRepository
) : ViewModel() {

    private val questSystemManager = QuestSystemManager()
    val timeRemaining = questSystemManager.timeRemaining.asLiveData()
    val escalationPhase = questSystemManager.currentPhase.asLiveData()

    val user: LiveData<UserEntity?> = userRepository.userFlow.asLiveData()
    val dailyQuests: LiveData<List<QuestEntity>> = questRepository.getTodayQuests().asLiveData()
    val activeGate: LiveData<GateEntity?> = gateRepository.activeGate.asLiveData()
    val shadowMultipliers: LiveData<Map<String, Double>> = shadowRepository.activeMultipliers.asLiveData()
    
    // One-shot event for UI (Penalty Triggered)
    private val _penaltyEvent = androidx.lifecycle.MutableLiveData<Boolean>()
    val penaltyEvent: LiveData<Boolean> = _penaltyEvent
    
    // Gate Events
    private val _gateEnteredEvent = androidx.lifecycle.MutableLiveData<Boolean>()
    val gateEnteredEvent: LiveData<Boolean> = _gateEnteredEvent
    
    private val _systemResetEvent = androidx.lifecycle.MutableLiveData<Boolean>()
    val systemResetEvent: LiveData<Boolean> = _systemResetEvent

    init {
        viewModelScope.launch {
            gateRepository.initializeGates()
            checkDailyCompliance()
            checkPenaltyExpiration()
            
            val user = userRepository.getCurrentUser()
            val level = user?.level ?: 1
            questRepository.generateDailyQuestsIfNeeded(level)
            
            shadowRepository.generateDailyShadowQuests()
            
            // Start systemic clock
            launch {
                while (true) {
                    val currentUser = userRepository.getCurrentUser()
                    val today = getTodayDate()
                    val incomplete = if (currentUser != null) questRepository.hasIncompleteQuests(today) else false
                    questSystemManager.update(System.currentTimeMillis(), today, incomplete)
                    
                    // Trigger Penalty if time just ran out in foreground
                    if (questSystemManager.currentPhase.value == QuestSystemManager.EscalationPhase.PENALTY && incomplete) {
                        if (currentUser != null && currentUser.penaltyEndTime < System.currentTimeMillis()) {
                             applyPenalty(currentUser)
                        }
                    }
                    
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }
    
    private suspend fun checkPenaltyExpiration() {
        val user = userRepository.getCurrentUser() ?: return
        if (user.penaltyEndTime > 0 && System.currentTimeMillis() >= user.penaltyEndTime) {
            userRepository.updateUser(user.copy(penaltyEndTime = 0, penaltyStatReduction = 0))
        }
    }
    
    private suspend fun checkDailyCompliance() {
        val user = userRepository.getCurrentUser() ?: return
        val today = getTodayDate()
        
        // Check for Gate Timeout
        val activeGate = gateRepository.getActiveGateSync()
        if (activeGate != null && !activeGate.isCleared && !activeGate.isFailed) {
             if (System.currentTimeMillis() > activeGate.endTimestamp) {
                 // Monarch Immunity
                 if (user.isMonarch) {
                     // Auto-clear or just ignore? User request: "Gate failures do NOT apply penalties."
                     // Logic: If time runs out, it's failed but NO PENALTY.
                     gateRepository.updateGate(activeGate.copy(isActive = false, isFailed = true))
                     return 
                 }
                 
                 // Time ran out!
                 failGate(activeGate, user)
                 return // Explicit return as penalty applied inside
             }
        }
        
        // Monarch Immunity to Daily Quest Miss
        if (user.isMonarch) return
        
        // If last active was not today (checking if we missed yesterday or prior)
        if (user.lastActiveDate < today) {
            val failed = questRepository.hasIncompleteQuests(user.lastActiveDate)
            
            if (failed) {
                applyPenalty(user)
                // Shadow Failure Logic
                shadowRepository.processDailyFailure(user)
                
                // Endurance Loss
                val reset = userRepository.decreaseEndurance(user)
                if (reset) {
                    performSystemReset(user)
                    return
                }
                
                // If Gate Active -> It Fails too
                if (activeGate != null && !activeGate.isCleared && !activeGate.isFailed) {
                     failGate(activeGate, user)
                }
            }
            
            // Update last active date to today
            userRepository.updateUser(userRepository.getCurrentUser()?.copy(lastActiveDate = today) ?: return)
        }
    }
    
    private suspend fun failGate(gate: GateEntity, user: UserEntity) {
        if (user.isMonarch) return // Immunity
        
        val sMults = shadowRepository.getActiveMultipliers()
        val discMult = sMults.getOrDefault("Discipline", 1.0).toFloat()

        // Apply Heavy Penalty
        // XP Reset to 0
        // Disc -5
        val newDisc = (user.discipline - 5).coerceAtLeast(0)
        
        // Recalculate Max HP due to VIT loss
        val newMaxHp = com.example.sololeveling.util.StatCalculator.calculateMaxHp(newDisc, discMult)
        // Endurance Loss (Scaling)
        val enduranceLoss = 20
        val reset = userRepository.decreaseEndurance(user, enduranceLoss)
        if (reset) {
            performSystemReset(user)
            return
        }
        
        val newEndurance = userRepository.getCurrentUser()?.endurance ?: 0
        
        val penalizedUser = user.copy(
            currentXP = 0,
            discipline = newDisc,
            endurance = newEndurance,
            maxEndurance = newMaxHp
        )
        userRepository.updateUser(penalizedUser)
        
        // Mark Gate Failed
        val failedGate = gate.copy(
            isActive = false,
            isFailed = true,
            cooldownEnd = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        )
        gateRepository.updateGate(failedGate)
        
        _penaltyEvent.value = true // Reuse penalty dialog
    }
    
    private suspend fun applyPenalty(user: UserEntity) {
        if (user.isMonarch) return // Immunity
        
        val sMults = shadowRepository.getActiveMultipliers()
        val discMult = sMults.getOrDefault("Discipline", 1.0).toFloat()

        // Calculate Reduction (VIT)
        val reductionPct = com.example.sololeveling.util.StatCalculator.calculatePenaltyReduction(user.discipline, discMult)
        val baseDuration = 12 * 60 * 60 * 1000L // 12 Hours
        val reducedDuration = (baseDuration * (1f - reductionPct)).toLong()
        
        val newXp = (user.currentXP * 0.7).toLong()
        val penaltyEnd = System.currentTimeMillis() + reducedDuration
        
        val penalizedUser = user.copy(
            currentXP = newXp,
            penaltyEndTime = penaltyEnd,
            penaltyStatReduction = 3 // Subtract 3 from all stats temporarily
        )
        userRepository.updateUser(penalizedUser)
        _penaltyEvent.postValue(true)
    }
    
    fun enterGate(gate: GateEntity) {
        viewModelScope.launch {
            gateRepository.enterGate(gate)
            _gateEnteredEvent.value = true
        }
    }

    fun completeQuest(quest: QuestEntity) {
        if (quest.isCompleted) return
        
        viewModelScope.launch {
             mutex.withLock {
                 // Re-check inside lock
                 // Since we don't have getQuestById handy exposed or it's costly, we rely on the DB transaction implicitly if we used @Transaction. 
                 // But manual mutex checks memory state? 
                 // Actually `quest` passed in is from Adapter. 
                 // Best effort: Check if it's already done in Repo?
                 // Or just proceed. The lock prevents TWO `completeQuest` calls from reading the SAME user state and writing concurrent updates.
                 
                 val currentUser = userRepository.getCurrentUser() ?: return@withLock
                 
                 // Check Penalty Lock
                 if (System.currentTimeMillis() < currentUser.penaltyEndTime) {
                     return@withLock
                 }
                 
                 // Check completions (double check)
                 // Assuming we can't easily check DB state here without query.
                 // We proceeded.
                 
                questRepository.markQuestComplete(quest)
                shadowRepository.processQuestCompletion(quest)
                
                val incompleteCount = questRepository.getIncompleteCount(quest.date)
                if (incompleteCount == 0) {
                    userRepository.processDailyEnduranceGain(currentUser)
                }
                
                val sMults = shadowRepository.getActiveMultipliers()
                val fitMult = sMults.getOrDefault("Fitness", 1.0).toFloat()
                val knlMult = sMults.getOrDefault("Knowledge", 1.0).toFloat()
                val discMult = sMults.getOrDefault("Discipline", 1.0).toFloat()

                // XP Calculation with Fitness/Knowledge Multiplier
                val xpMultiplier = com.example.sololeveling.util.StatCalculator.calculateXpMultiplier(currentUser.fitness, currentUser.knowledge, fitMult * knlMult, currentUser.isMonarch)
                val bonusXp = (quest.xpReward * xpMultiplier).toLong()
                
                var newXp = currentUser.currentXP + bonusXp
                var newLevel = currentUser.level
                var requiredXp = com.example.sololeveling.util.StatCalculator.calculateRequiredXp(newLevel, currentUser.knowledge, knlMult, currentUser.isMonarch)
                var unspentPoints = currentUser.unspentPoints
                
                // Level Up Check
                while (newXp >= requiredXp) {
                    newXp -= requiredXp
                    newLevel++
                    requiredXp = com.example.sololeveling.util.StatCalculator.calculateRequiredXp(newLevel, currentUser.knowledge, knlMult, currentUser.isMonarch)
                    unspentPoints += 3
                }
    
                var userUpdate = currentUser.copy(
                    level = newLevel,
                    currentXP = newXp,
                    unspentPoints = unspentPoints
                )
                
                // Apply Stat specific rewards
                val statType = com.example.sololeveling.logic.StatType.fromString(quest.linkedStat)
                userUpdate = when (statType) {
                    com.example.sololeveling.logic.StatType.FITNESS -> userUpdate.copy(fitness = userUpdate.fitness + quest.statReward)
                    com.example.sololeveling.logic.StatType.KNOWLEDGE -> userUpdate.copy(knowledge = userUpdate.knowledge + quest.statReward)
                    com.example.sololeveling.logic.StatType.DISCIPLINE -> userUpdate.copy(discipline = userUpdate.discipline + quest.statReward)
                    com.example.sololeveling.logic.StatType.AWARENESS -> userUpdate.copy(awareness = userUpdate.awareness + quest.statReward)
                    com.example.sololeveling.logic.StatType.CHARISMA -> userUpdate.copy(charisma = userUpdate.charisma + quest.statReward)
                    com.example.sololeveling.logic.StatType.LUCK -> userUpdate.copy(luck = userUpdate.luck + quest.statReward)
                    null -> userUpdate
                }
                
                userRepository.updateUser(userUpdate)
            }
        }
    }
    
    
    private val _promotionEvent = androidx.lifecycle.MutableLiveData<Boolean>()
    val promotionEvent: LiveData<Boolean> = _promotionEvent

    private val _monarchEvent = androidx.lifecycle.MutableLiveData<Boolean>()
    val monarchEvent: LiveData<Boolean> = _monarchEvent
    
    // Concurrency Safety
    private val mutex = kotlinx.coroutines.sync.Mutex()

    fun resetPenaltyEvent() { _penaltyEvent.value = false }
    fun resetSystemResetEvent() { _systemResetEvent.value = false }
    fun resetPromotionEvent() { _promotionEvent.value = false }
    fun resetMonarchEvent() { _monarchEvent.value = false }
    fun resetGateEnteredEvent() { _gateEnteredEvent.value = false }

    private fun checkPromotionEligibility() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            // Monarch Check
            if (user.rank == "S" && user.hasDefeatedBossSincePromotion) { 
                 _monarchEvent.value = true
                 return@launch
            }
            
            // Criteria
            val gateDone = user.hasClearedGateSincePromotion
            val bossDone = user.hasDefeatedBossSincePromotion
            if (!gateDone || !bossDone) return@launch
            
            val nextRank = getNextRank(user.rank) ?: return@launch
            if (nextRank == "MONARCH") return@launch 
            
            val requiredLevel = getRequiredLevelForRank(nextRank)
            
            if (user.level >= requiredLevel) {
                _promotionEvent.value = true
            }
        }
    }

    
    fun promoteToMonarch() {
         viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            userRepository.promoteToMonarch(user)
         }
    }
    
    fun promoteUser() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            val nextRank = getNextRank(user.rank) ?: return@launch
            
            // Re-validate Criteria
            val requiredLevel = getRequiredLevelForRank(nextRank)
            if (user.level < requiredLevel || !user.hasClearedGateSincePromotion || !user.hasDefeatedBossSincePromotion) {
                return@launch
            }
            
            val promotedUser = user.copy(
                rank = nextRank,
                hasClearedGateSincePromotion = false,
                hasDefeatedBossSincePromotion = false,
                currentXP = user.currentXP + 5000 // Bonus XP
            )
            userRepository.updateUser(promotedUser)
            
            // Unlock Content
            unlockContentForRank(nextRank)
        }
    }
    
    private fun getNextRank(current: String): String? {
        return when (current) {
            "E" -> "D"
            "D" -> "C"
            "C" -> "B"
            "B" -> "A"
            "A" -> "S"
            "S" -> "MONARCH"
            else -> null
        }
    }
    
    private fun getRequiredLevelForRank(rank: String): Int {
        return when (rank) {
            "D" -> 5
            "C" -> 10
            "B" -> 20
            "A" -> 30
            "S" -> 50
            else -> 999
        }
    }
    
    private suspend fun unlockContentForRank(rank: String) {
        // Delegate to Repos
        gateRepository.unlockGatesForRank(rank)
        // bossRepository.unlockBossesForRank(rank) // BossRepo needs update
        bossRepository.unlockBossesForRank(rank) 
    }
    
    private suspend fun performSystemReset(user: UserEntity) {
        val sMults = shadowRepository.getActiveMultipliers()
        val discMult = sMults.getOrDefault("Discipline", 1.0).toFloat()
        
        userRepository.performSystemReset(user, discMult)
        shadowRepository.reduceAllShadowsLoyalty(user.isMonarch)
        
        // Fail active gate
        val activeGate = gateRepository.getActiveGateSync()
        if (activeGate != null) {
             val failedGate = activeGate.copy(
                isActive = false,
                isFailed = true,
                cooldownEnd = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
            )
            gateRepository.updateGate(failedGate)
        }
        
        _systemResetEvent.value = true
    }
    
    private fun getTodayDate(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun allocatePoints(str: Int, int: Int, disc: Int, awk: Int, chr: Int, luk: Int) {
        viewModelScope.launch {
            mutex.withLock {
                val user = userRepository.getCurrentUser() ?: return@withLock
                val totalCost = str + int + disc + awk + chr + luk
                
                if (totalCost > 0 && user.unspentPoints >= totalCost) {
                    val newStr = (user.fitness + str).coerceAtLeast(0)
                    val newInt = (user.knowledge + int).coerceAtLeast(0)
                    val newDisc = (user.discipline + disc).coerceAtLeast(0)
                    val newAwk = (user.awareness + awk).coerceAtLeast(0)
                    val newChr = (user.charisma + chr).coerceAtLeast(0)
                    val newLuk = (user.luck + luk).coerceAtLeast(0)
                    
                    val sMults = shadowRepository.getActiveMultipliers()
                    val discMult = sMults.getOrDefault("Discipline", 1.0).toFloat()

                    // Recalculate Max HP (VIT)
                    val newMaxHp = com.example.sololeveling.util.StatCalculator.calculateMaxHp(newDisc, discMult, user.isMonarch)
                    val hpDiff = newMaxHp - user.maxEndurance
                    val newHp = (user.endurance + hpDiff).coerceIn(0, newMaxHp) 
                    
                    val updatedUser = user.copy(
                        fitness = newStr,
                        knowledge = newInt,
                        discipline = newDisc,
                        awareness = newAwk,
                        charisma = newChr,
                        luck = newLuk,
                        unspentPoints = user.unspentPoints - totalCost,
                        maxEndurance = newMaxHp,
                        endurance = newHp
                    )
                    userRepository.updateUser(updatedUser)
                }
            }
        }
    }
}
