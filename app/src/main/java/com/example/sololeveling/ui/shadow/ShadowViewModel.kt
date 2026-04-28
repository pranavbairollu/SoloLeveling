package com.example.sololeveling.ui.shadow

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.sololeveling.data.entity.ShadowEntity
import com.example.sololeveling.data.repository.ShadowRepository

class ShadowViewModel(
    private val shadowRepository: ShadowRepository
) : ViewModel() {
    val allShadows: LiveData<List<ShadowEntity>> = shadowRepository.allShadows.asLiveData()
}
