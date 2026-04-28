package com.example.sololeveling.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: UserRepository) : ViewModel() {

    private val _bootMessage = MutableLiveData<String>()
    val bootMessage: LiveData<String> = _bootMessage

    private val _showAcceptButton = MutableLiveData<Boolean>()
    val showAcceptButton: LiveData<Boolean> = _showAcceptButton

    private val _navigationEvent = MutableLiveData<Boolean>()
    val navigationEvent: LiveData<Boolean> = _navigationEvent

    init {
        startBootSequence()
    }

    private fun startBootSequence() {
        viewModelScope.launch {
            val messages = listOf(
                "SYSTEM ONLINE",
                "PLAYER DETECTED",
                "AWAKENING STATUS: FAILED",
                "SPECIAL CONDITION MET",
                "SYSTEM GRANTED"
            )

            for (msg in messages) {
                _bootMessage.value = msg
                delay(1200) // Typewriter timing illusion
            }
            
            checkUserAndPrompt()
        }
    }

    private suspend fun checkUserAndPrompt() {
        // If user already exists, maybe skip accept? 
        // Requirement says "System messages displayed sequentially... User must accept System Authority".
        // Use logic: if user exists, maybe already accepted.
        // For now, always show flow if it's the "First Launch Flow".
        
        val user = repository.getCurrentUser()
        if (user == null) {
            _showAcceptButton.value = true
        } else {
            // Already initialized, go to dashboard
             delay(1000)
            _navigationEvent.value = true
        }
    }

    fun onAcceptClicked() {
        viewModelScope.launch {
            val newUser = UserEntity() // Default Level 1, Rank E
            repository.insertUser(newUser)
            _navigationEvent.value = true
        }
    }
}
