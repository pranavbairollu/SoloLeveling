package com.example.sololeveling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sololeveling.data.entity.ShadowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShadowDao {
    @Query("SELECT * FROM shadow_table ORDER BY id ASC")
    fun getAllShadows(): Flow<List<ShadowEntity>>
    
    @Query("SELECT * FROM shadow_table WHERE isActive = 1")
    suspend fun getActiveShadowsSync(): List<ShadowEntity>

    @Query("SELECT * FROM shadow_table WHERE sourceBossId = :bossId LIMIT 1")
    suspend fun getShadowByBossId(bossId: Int): ShadowEntity?

    @Query("SELECT * FROM shadow_table WHERE id = :shadowId")
    suspend fun getShadowById(shadowId: Int): ShadowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShadow(shadow: ShadowEntity)

    @Update
    suspend fun updateShadow(shadow: ShadowEntity)
}
