package com.example.sololeveling.data.repository

import com.example.sololeveling.data.dao.MonarchDao
import com.example.sololeveling.data.entity.MonarchEntity
import kotlinx.coroutines.flow.Flow

class MonarchRepository(private val monarchDao: MonarchDao) {

    val monarch: Flow<MonarchEntity?> = monarchDao.getMonarch()

    suspend fun createOrUpdateMonarch(title: String, victoryCondition: String, requiredStats: Int) {
        val monarch = MonarchEntity(
            title = title,
            victoryCondition = victoryCondition,
            requiredAggregateStats = requiredStats
        )
        monarchDao.insertMonarch(monarch)
    }

    suspend fun markCompleted() {
        monarchDao.completeMonarch()
    }
}
