package com.example.sololeveling.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class QuestSystemManager {

    enum class EscalationPhase {
        NONE,       // > 2h
        PHASE_1,    // 2h to 30m (Subtle)
        PHASE_2,    // 30m to 10m (Intense)
        PHASE_3,    // < 10m (Critical - Haptics + Red Border)
        PENALTY     // Midnight passed and quests incomplete
    }

    private val _timeRemaining = MutableStateFlow(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining

    private val _currentPhase = MutableStateFlow(EscalationPhase.NONE)
    val currentPhase: StateFlow<EscalationPhase> = _currentPhase

    /**
     * Updates the system state based on current time and whether quests for the target date are complete.
     */
    fun update(currentTimeMillis: Long, targetMissionDate: Long, areQuestsIncomplete: Boolean) {
        val deadline = targetMissionDate + (24 * 60 * 60 * 1000L)
        val diff = deadline - currentTimeMillis

        _timeRemaining.value = diff.coerceAtLeast(0)

        if (!areQuestsIncomplete) {
            _currentPhase.value = EscalationPhase.NONE
            return
        }

        _currentPhase.value = when {
            diff <= 0 -> EscalationPhase.PENALTY
            diff < 10 * 60 * 1000 -> EscalationPhase.PHASE_3
            diff < 30 * 60 * 1000 -> EscalationPhase.PHASE_2
            diff < 2 * 60 * 60 * 1000 -> EscalationPhase.PHASE_1
            else -> EscalationPhase.NONE
        }
    }

    private fun getMidnightTimestamp(now: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    fun getFormattedTime(diff: Long): String {
        if (diff <= 0) return "[ TIME REMAINING: 00:00:00 ]"
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        val seconds = (diff / 1000) % 60
        return String.format("[ TIME REMAINING: %02d:%02d:%02d ]", hours, minutes, seconds)
    }
}
