package com.example.sololeveling.ui.boss

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.sololeveling.data.entity.BossEntity
import com.example.sololeveling.data.repository.BossRepository
import com.example.sololeveling.data.repository.UserRepository
import kotlinx.coroutines.launch

import com.example.sololeveling.data.repository.ShadowRepository

class BossViewModel(
    private val userRepository: UserRepository,
    private val bossRepository: BossRepository,
    private val shadowRepository: ShadowRepository
) : ViewModel() {

    val allBosses: LiveData<List<BossEntity>> = bossRepository.allBosses.asLiveData()
    
    // Status/Dialog events
    private val _messageEvent = MutableLiveData<String>()
    val messageEvent: LiveData<String> = _messageEvent
    
    private val _bossResultEvent = MutableLiveData<BossResult>()
    val bossResultEvent: LiveData<BossResult> = _bossResultEvent
    
    enum class Qualification {
        QUALIFIED, HIGH_RISK, UNQUALIFIED, ON_COOLDOWN, NO_GATES
    }

    sealed class BossResult {
        object Victory : BossResult()
        object Defeat : BossResult()
        data class QualificationInfo(val boss: BossEntity, val q: Qualification, val missingStats: List<String>) : BossResult()
    }

    init {
        viewModelScope.launch {
            bossRepository.initializeBosses()
        }
    }

    fun checkBossQualification(boss: BossEntity) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            if (!bossRepository.hasClearedAnyGate()) {
                _bossResultEvent.value = BossResult.QualificationInfo(boss, Qualification.NO_GATES, emptyList())
                return@launch
            }
            
            if (System.currentTimeMillis() < boss.cooldownUntil) {
                _bossResultEvent.value = BossResult.QualificationInfo(boss, Qualification.ON_COOLDOWN, emptyList())
                return@launch
            }

            val missingStats = mutableListOf<String>()
            if (user.level < boss.requiredLevel) missingStats.add("Level ${boss.requiredLevel}")
            if (user.fitness < boss.requiredFitness) missingStats.add("Fitness ${boss.requiredFitness}")
            if (user.knowledge < boss.requiredKnowledge) missingStats.add("Knowledge ${boss.requiredKnowledge}")
            if (user.discipline < boss.requiredDiscipline) missingStats.add("Discipline ${boss.requiredDiscipline}")
            if (user.awareness < boss.requiredAwareness) missingStats.add("Awareness ${boss.requiredAwareness}")
            if (user.charisma < boss.requiredCharisma) missingStats.add("Charisma ${boss.requiredCharisma}")
            if (user.luck < boss.requiredLuck) missingStats.add("Luck ${boss.requiredLuck}")

            val q = when {
                missingStats.isEmpty() -> Qualification.QUALIFIED
                user.level >= boss.requiredLevel && missingStats.size <= 2 -> Qualification.HIGH_RISK
                else -> Qualification.UNQUALIFIED
            }
            
            _bossResultEvent.value = BossResult.QualificationInfo(boss, q, missingStats)
        }
    }

    fun startRaid(boss: BossEntity) {
        viewModelScope.launch {
            val active = bossRepository.getActiveBoss()
            if (active != null) {
                _messageEvent.value = "A RAID IS ALREADY IN PROGRESS: ${active.name}"
                return@launch
            }
            
            bossRepository.updateBoss(boss.copy(isActive = true))
            _messageEvent.value = "RAID COMMENCED: ${boss.name}. THE SYSTEM IS WATCHING."
        }
    }

    fun resolveRaid(boss: BossEntity, success: Boolean) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            if (success) {
                // Victory Logic
                val newXp = user.currentXP + boss.xpReward
                val userUpdate = user.copy(
                    currentXP = newXp,
                    hasDefeatedBossSincePromotion = true
                )
                userRepository.updateUser(userUpdate)
                
                bossRepository.updateBoss(boss.copy(
                    isDefeated = true, 
                    isActive = false, 
                    defeatDate = System.currentTimeMillis()
                ))
                
                shadowRepository.extractShadow(boss)
                _messageEvent.value = "MILESTONE ACHIEVED: ${boss.name}. SHADOW EXTRACTED."
                _bossResultEvent.value = BossResult.Victory
            } else {
                // Failure Logic
                if (user.isMonarch) {
                    bossRepository.updateBoss(boss.copy(isActive = false))
                    _messageEvent.value = "RAID FAILED. BUT THE MONARCH DOES NOT BEND."
                    _bossResultEvent.value = BossResult.Defeat
                    return@launch
                }

                // Penalties
                val pDisc = (user.discipline * 0.9).toInt().coerceAtLeast(0)
                val newMaxHp = com.example.sololeveling.util.StatCalculator.calculateMaxHp(pDisc)
                val newEnd = (user.endurance * 0.5).toInt().coerceAtMost(newMaxHp).coerceAtLeast(0)
                
                val penalized = user.copy(
                    discipline = pDisc,
                    maxEndurance = newMaxHp,
                    endurance = newEnd,
                    currentXP = (user.currentXP * 0.8).toLong() // 20% XP Loss
                )
                userRepository.updateUser(penalized)
                
                val cooldown = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                bossRepository.updateBoss(boss.copy(isActive = false, cooldownUntil = cooldown))
                
                _messageEvent.value = "DEFEAT! THE SYSTEM HAS IMPOSED A 7-DAY PENALTY."
                _bossResultEvent.value = BossResult.Defeat
            }
        }
    }
}
