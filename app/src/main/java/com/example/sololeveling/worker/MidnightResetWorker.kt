package com.example.sololeveling.worker

import android.content.Context
import androidx.work.*
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.repository.QuestRepository
import com.example.sololeveling.data.repository.UserRepository
import java.util.*
import java.util.concurrent.TimeUnit

class MidnightResetWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = SystemDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(database.userDao())
        val questRepository = QuestRepository(database.questDao())

        val user = userRepository.getCurrentUser() ?: return Result.success()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Check yesterday
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val yesterday = calendar.timeInMillis

        val hasIncomplete = questRepository.hasIncompleteQuests(yesterday)
        
        if (hasIncomplete && !user.isMonarch) {
            // Apply Penalty
            val baseDuration = 12 * 60 * 60 * 1000L
            val reductionPct = com.example.sololeveling.util.StatCalculator.calculatePenaltyReduction(user.discipline)
            val reducedDuration = (baseDuration * (1f - reductionPct)).toLong()
            
            val newXp = (user.currentXP * 0.7).toLong()
            val penaltyEnd = System.currentTimeMillis() + reducedDuration
            
            userRepository.updateUser(user.copy(
                currentXP = newXp,
                penaltyEndTime = penaltyEnd,
                penaltyStatReduction = 3,
                lastActiveDate = System.currentTimeMillis() // Mark today as active now
            ))
        }

        // Schedule next one
        schedule(applicationContext)

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "MidnightResetWorker"

        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val nextMidnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 1)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val delay = nextMidnight.timeInMillis - now.timeInMillis

            val request = OneTimeWorkRequestBuilder<MidnightResetWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
