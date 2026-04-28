package com.example.sololeveling.ui.gate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.sololeveling.data.entity.GateEntity
import com.example.sololeveling.data.repository.GateRepository
import com.example.sololeveling.data.repository.QuestRepository
import com.example.sololeveling.data.repository.UserRepository
import kotlinx.coroutines.launch

class GateViewModel(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val gateRepository: GateRepository
) : ViewModel() {

    val allGates: LiveData<List<GateEntity>> = gateRepository.allGates.asLiveData()
    val activeGate: LiveData<GateEntity?> = gateRepository.activeGate.asLiveData()
    
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    fun attemptEnterGate(gate: GateEntity) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            // Check Access
            if (user.level < gate.requiredLevel) {
                _errorEvent.value = "LEVEL TOO LOW"
                return@launch
            }
            
            // Check Penalty
            if (System.currentTimeMillis() < user.penaltyEndTime) {
                 _errorEvent.value = "PENALTY ACTIVE - ENTRY DENIED"
                 return@launch
            }
            
            // Check if another gate active (should be handled by UI visibility, but double check)
            val currentActive = gateRepository.getActiveGateSync()
            if (currentActive != null) {
                _errorEvent.value = "ALREADY IN A GATE"
                return@launch
            }
            
            // Enter
            gateRepository.enterGate(gate)
        }
    }
}
