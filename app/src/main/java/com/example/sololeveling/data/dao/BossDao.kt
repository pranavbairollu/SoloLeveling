package com.example.sololeveling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sololeveling.data.entity.BossEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BossDao {
    @Query("SELECT * FROM boss_table ORDER BY requiredLevel ASC")
    fun getAllBosses(): Flow<List<BossEntity>>

    @Query("SELECT * FROM boss_table WHERE id = :id")
    suspend fun getBossById(id: Int): BossEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBosses(bosses: List<BossEntity>)

    @Query("SELECT COUNT(*) FROM boss_table")
    suspend fun getBossCount(): Int

    @Query("SELECT * FROM boss_table WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveBoss(): BossEntity?

    @Update
    suspend fun updateBoss(boss: BossEntity)

    @Query("UPDATE boss_table SET isUnlocked = 1 WHERE rank = :rank")
    suspend fun unlockBossesForRank(rank: String): Int
}
