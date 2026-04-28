package com.example.sololeveling.data.dao

import androidx.room.*
import com.example.sololeveling.data.entity.MonarchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonarchDao {
    @Query("SELECT * FROM monarch_table WHERE id = 1 LIMIT 1")
    fun getMonarch(): Flow<MonarchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonarch(monarch: MonarchEntity)
    
    @Update
    suspend fun updateMonarch(monarch: MonarchEntity)
    
    @Query("UPDATE monarch_table SET isCompleted = 1 WHERE id = 1")
    suspend fun completeMonarch()
}
