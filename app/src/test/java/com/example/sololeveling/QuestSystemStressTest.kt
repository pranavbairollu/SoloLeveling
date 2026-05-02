package com.example.sololeveling

import com.example.sololeveling.util.QuestSystemManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuestSystemStressTest {

    private lateinit var manager: QuestSystemManager

    @Before
    fun setup() {
        manager = QuestSystemManager()
    }

    @Test
    fun `test escalation phases`() {
        val now = System.currentTimeMillis()
        val targetMidnight = getMidnight(now)
        val today = targetMidnight - (24 * 60 * 60 * 1000L)
        
        // 5 hours remaining -> NONE
        manager.update(now, today, true)
        assertEquals(QuestSystemManager.EscalationPhase.NONE, manager.currentPhase.value)
        
        // 1.5 hours remaining -> PHASE_1
        manager.update(targetMidnight - (90 * 60 * 1000), today, true) // 90 mins left
        assertEquals(QuestSystemManager.EscalationPhase.PHASE_1, manager.currentPhase.value)
        
        manager.update(targetMidnight - (20 * 60 * 1000), today, true) // 20 mins left
        assertEquals(QuestSystemManager.EscalationPhase.PHASE_2, manager.currentPhase.value)
        
        manager.update(targetMidnight - (5 * 60 * 1000), today, true) // 5 mins left
        assertEquals(QuestSystemManager.EscalationPhase.PHASE_3, manager.currentPhase.value)
        
        manager.update(targetMidnight + 1000, today, true) // 1 sec past midnight
        assertEquals(QuestSystemManager.EscalationPhase.PENALTY, manager.currentPhase.value)
    }

    @Test
    fun `test no escalation if quests complete`() {
        val now = System.currentTimeMillis()
        val targetMidnight = getMidnight(now)
        val today = targetMidnight - (24 * 60 * 60 * 1000L)
        
        manager.update(targetMidnight - (5 * 60 * 1000), today, false) // 5 mins left, but DONE
        assertEquals(QuestSystemManager.EscalationPhase.NONE, manager.currentPhase.value)
    }

    @Test
    fun `test formatted time`() {
        val fiveMins = 5 * 60 * 1000L
        assertEquals("[ TIME REMAINING: 00:05:00 ]", manager.getFormattedTime(fiveMins))
        
        val zero = 0L
        assertEquals("[ TIME REMAINING: 00:00:00 ]", manager.getFormattedTime(zero))
    }

    private fun getMidnight(now: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }
}
