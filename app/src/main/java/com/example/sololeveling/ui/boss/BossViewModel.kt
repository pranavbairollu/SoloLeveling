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
    
    // Result events
    private val _bossResultEvent = MutableLiveData<BossResult>()
    val bossResultEvent: LiveData<BossResult> = _bossResultEvent
    
    sealed class BossResult {
        object Victory : BossResult()
        object Defeat : BossResult()
    }

    init {
        viewModelScope.launch {
            bossRepository.initializeBosses()
        }
    }

    fun engageBoss(boss: BossEntity) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            // 1. Check if Gate Cleared
            if (!bossRepository.hasClearedAnyGate()) {
                _messageEvent.value = "YOU ARE NOT QUALIFIED (NO GATES CLEARED)"
                return@launch
            }
            
            // 2. Check Cooldown
            if (System.currentTimeMillis() < boss.cooldownUntil) {
                 _messageEvent.value = "BOSS IS RECOVERING (COOLDOWN)"
                 return@launch
            }
             
            // 3. Validate Requirements
            val isQualified = (user.level >= boss.requiredLevel) &&
                              (user.fitness >= boss.requiredFitness) &&
                              (user.knowledge >= boss.requiredKnowledge) &&
                              (user.discipline >= boss.requiredDiscipline)
            
            if (isQualified) {
                // Success Logic
                // Grant Reward
                val newXp = user.currentXP + boss.xpReward
                // Assuming level up handled elsewhere or simplistic add
                // Ideally, re-use level up logic. For now, just add.
                
                val userUpdate = user.copy(currentXP = newXp) // Add logic for promotion later
                userRepository.updateUser(userUpdate)
                
                // Mark Boss Defeated
                bossRepository.updateBoss(boss.copy(isDefeated = true))
                
                // Shadow Extraction
                shadowRepository.extractShadow(boss)
                
                _messageEvent.value = "BOSS DEFEATED: ${boss.name}. SHADOW EXTRACTION SUCCESSFUL."
                _bossResultEvent.value = BossResult.Victory
            } else {
                // Failure Logic
                if (user.isMonarch) {
                    _messageEvent.value = "DEFEAT! BUT THE SYSTEM OBEYS. NO PENALTY."
                    _bossResultEvent.value = BossResult.Defeat // Just return defeat without penalty
                    return@launch
                }

                // Penalties
                val pXp = 0L
                val pDisc = (user.discipline - 5).coerceAtLeast(0)
                
                // Recalculate Max HP due to VIT loss
                val newMaxHp = com.example.sololeveling.util.StatCalculator.calculateMaxHp(pDisc)
                
                // Endurance Loss
                val reset = userRepository.decreaseEndurance(user)
                if (reset) {
                    userRepository.performSystemReset(user)
                    _messageEvent.value = "SYSTEM RESET TRIGGERED due to Endurance Failure."
                }
                
                val penalized = if (reset) {
                    userRepository.getCurrentUser()!! // Reload fresh reset user
                } else {
                    // Update stats
                    val newEnd = user.endurance.coerceAtMost(newMaxHp).coerceAtLeast(0)
                    user.copy(
                        currentXP = pXp, 
                        discipline = pDisc,
                        maxEndurance = newMaxHp,
                        endurance = newEnd 
                    )
                }
                userRepository.updateUser(penalized)
                
                // Boss Cooldown
                val cooldown = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                bossRepository.updateBoss(boss.copy(cooldownUntil = cooldown))
                
                _messageEvent.value = "DEFEAT! WEAKNESS DETECTED."
                _bossResultEvent.value = BossResult.Defeat
            }
        }
    }
}
