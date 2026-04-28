package com.example.sololeveling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sololeveling.data.entity.GateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GateDao {
    @Query("SELECT * FROM gate_table ORDER BY requiredLevel ASC")
    fun getAllGates(): Flow<List<GateEntity>>

    @Query("SELECT * FROM gate_table WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveGate(): GateEntity?

    @Query("SELECT * FROM gate_table WHERE id = :gateId")
    suspend fun getGateById(gateId: Int): GateEntity?
    
    @Query("SELECT * FROM gate_table WHERE isActive = 1 LIMIT 1")
    fun getActiveGateFlow(): Flow<GateEntity?>

    @Query("SELECT COUNT(*) FROM gate_table WHERE isCleared = 1")
    suspend fun getClearedGateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGate(gate: GateEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGates(gates: List<GateEntity>)

    @Update
    suspend fun updateGate(gate: GateEntity)
}
