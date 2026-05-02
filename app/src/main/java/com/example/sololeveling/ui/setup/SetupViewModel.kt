package com.example.sololeveling.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sololeveling.data.entity.*
import com.example.sololeveling.data.repository.*
import com.example.sololeveling.logic.ClassManager
import com.example.sololeveling.logic.SupportedClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SetupState {
    object Idle : SetupState()
    object Saving : SetupState()
    object Success : SetupState()
    data class Error(val message: String) : SetupState()
}

class SetupViewModel(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val gateRepository: GateRepository,
    private val bossRepository: BossRepository,
    private val shadowRepository: ShadowRepository,
    private val monarchRepository: MonarchRepository
) : ViewModel() {

    // PHASE 1: Role Selection
    private val _selectedClass = MutableStateFlow<SupportedClass>(SupportedClass.CUSTOM)
    val selectedClass: StateFlow<SupportedClass> = _selectedClass.asStateFlow()

    // PHASE 2: Review & Edit (Mutable State)
    private val _quests = MutableStateFlow<List<QuestEntity>>(emptyList())
    val quests: StateFlow<List<QuestEntity>> = _quests.asStateFlow()

    private val _gates = MutableStateFlow<List<GateEntity>>(emptyList())
    val gates: StateFlow<List<GateEntity>> = _gates.asStateFlow()

    private val _bosses = MutableStateFlow<List<BossEntity>>(emptyList())
    val bosses: StateFlow<List<BossEntity>> = _bosses.asStateFlow()

    private val _shadows = MutableStateFlow<List<ShadowEntity>>(emptyList())
    val shadows: StateFlow<List<ShadowEntity>> = _shadows.asStateFlow()
    
    // Monarch is single, but editable
    private val _monarch = MutableStateFlow<MonarchEntity?>(null)
    val monarch: StateFlow<MonarchEntity?> = _monarch.asStateFlow()

    // PHASE 3: State Management
    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    // Setup Functions
    fun selectClass(classType: SupportedClass) {
        _selectedClass.value = classType
        loadTemplates(classType)
    }

    private fun loadTemplates(classType: SupportedClass) {
        _quests.value = ClassManager.getDefaultQuestsForClass(classType)
        _gates.value = ClassManager.getDefaultGatesForClass(classType)
        _bosses.value = ClassManager.getDefaultBossesForClass(classType)
        _shadows.value = ClassManager.getDefaultShadowsForClass(classType)
        _monarch.value = ClassManager.getDefaultMonarchForClass(classType)
    }

    // Edit Functions (Phase 2)
    fun updateQuest(updatedQuest: QuestEntity) {
        _quests.value = _quests.value.map { if (it.title == updatedQuest.title) updatedQuest else it }
    }
    
    fun addQuest(quest: QuestEntity) {
        _quests.value = _quests.value + quest
    }

    fun removeQuest(quest: QuestEntity) {
        _quests.value = _quests.value - quest
    }

    fun updateGate(updatedGate: GateEntity) {
        _gates.value = _gates.value.map { if (it.id == updatedGate.id) updatedGate else it }
    }

    fun updateBoss(updatedBoss: BossEntity) {
         _bosses.value = _bosses.value.map { if (it.id == updatedBoss.id) updatedBoss else it }
    }

    fun updateShadow(updatedShadow: ShadowEntity) {
        _shadows.value = _shadows.value.map { if (it.name == updatedShadow.name) updatedShadow else it }
    }

    fun updateMonarch(updatedMonarch: MonarchEntity) {
        _monarch.value = updatedMonarch
    }

    // PHASE 0: Player Name
    private var playerName: String = ""

    fun setPlayerName(name: String) {
        playerName = name.trim()
    }

    // PHASE 3: The Contract (Confirmation)
    fun confirmSetup() {
        if (_setupState.value is SetupState.Saving || _setupState.value is SetupState.Success) return
        
        // Validation (Bulletproof)
        if (playerName.isBlank()) {
            _setupState.value = SetupState.Error("Player name missing. Please restart setup.")
            return
        }

        _setupState.value = SetupState.Saving
        
        viewModelScope.launch {
            try {
                // 1. Initialize User
                val statFocus = ClassManager.getClassStatFocus(_selectedClass.value)
                
                // Starting Stats with Class Modifiers
                var startFitness = 10
                var startKnowledge = 10
                var startDiscipline = 10
                var startAwareness = 10
                var startCharisma = 10
                var startLuck = 10

                statFocus.forEach { (priority, statName) ->
                    val bonus = if (priority == "Primary") 5 else 3
                    when (statName) {
                        "Fitness" -> startFitness += bonus
                        "Knowledge" -> startKnowledge += bonus
                        "Discipline" -> startDiscipline += bonus
                        "Awareness" -> startAwareness += bonus
                        "Charisma" -> startCharisma += bonus
                        "Luck" -> startLuck += bonus
                    }
                }

                val newUser = UserEntity(
                    id = 1, // Singleton
                    username = playerName,
                    level = 1,
                    rank = "E",
                    currentXP = 0,
                    unspentPoints = 0,
                    fitness = startFitness,
                    knowledge = startKnowledge,
                    discipline = startDiscipline,
                    awareness = startAwareness,
                    charisma = startCharisma,
                    luck = startLuck,
                    endurance = 100,
                    maxEndurance = 100,
                    onboardingCompleted = true
                ) 
                
                userRepository.insertUser(newUser)

                // 2. Persist Entities
                questRepository.insertQuests(_quests.value)
                gateRepository.insertGates(_gates.value)
                bossRepository.insertBosses(_bosses.value)
                shadowRepository.insertShadows(_shadows.value)
                
                _monarch.value?.let { 
                    monarchRepository.createOrUpdateMonarch(it.title, it.victoryCondition, it.requiredAggregateStats)
                }
                
                _setupState.value = SetupState.Success
            } catch (e: Exception) {
                _setupState.value = SetupState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
