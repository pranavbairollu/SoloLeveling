package com.example.sololeveling.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sololeveling.data.repository.GateRepository
import com.example.sololeveling.data.repository.QuestRepository
import com.example.sololeveling.data.repository.UserRepository
import com.example.sololeveling.data.repository.BossRepository
import com.example.sololeveling.data.repository.ShadowRepository
import com.example.sololeveling.ui.dashboard.MainViewModel
import com.example.sololeveling.ui.splash.SplashViewModel
import com.example.sololeveling.ui.gate.GateViewModel
import com.example.sololeveling.ui.boss.BossViewModel
import com.example.sololeveling.ui.shadow.ShadowViewModel

class SystemViewModelFactory(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val gateRepository: GateRepository,
    private val bossRepository: BossRepository,
    private val shadowRepository: ShadowRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(userRepository) as T
        }
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(userRepository, questRepository, gateRepository, bossRepository, shadowRepository) as T
        }
        if (modelClass.isAssignableFrom(GateViewModel::class.java)) {
            return GateViewModel(userRepository, questRepository, gateRepository) as T
        }
        if (modelClass.isAssignableFrom(BossViewModel::class.java)) {
            return BossViewModel(userRepository, bossRepository, shadowRepository) as T
        }
        if (modelClass.isAssignableFrom(ShadowViewModel::class.java)) {
            return ShadowViewModel(shadowRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
