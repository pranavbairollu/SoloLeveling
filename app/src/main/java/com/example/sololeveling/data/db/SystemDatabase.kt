package com.example.sololeveling.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sololeveling.data.dao.UserDao
import com.example.sololeveling.data.entity.UserEntity
import com.example.sololeveling.data.dao.QuestDao
import com.example.sololeveling.data.entity.QuestEntity

import com.example.sololeveling.data.dao.GateDao
import com.example.sololeveling.data.entity.GateEntity
import com.example.sololeveling.data.dao.BossDao
import com.example.sololeveling.data.entity.BossEntity
import com.example.sololeveling.data.dao.ShadowDao
import com.example.sololeveling.data.entity.ShadowEntity
import com.example.sololeveling.data.dao.MonarchDao
import com.example.sololeveling.data.entity.MonarchEntity
import com.example.sololeveling.util.SecurityUtils
import net.sqlcipher.database.SupportFactory

@Database(entities = [UserEntity::class, QuestEntity::class, GateEntity::class, BossEntity::class, ShadowEntity::class, MonarchEntity::class], version = 8, exportSchema = false)
abstract class SystemDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao
    abstract fun gateDao(): GateDao
    abstract fun bossDao(): BossDao
    abstract fun shadowDao(): ShadowDao
    abstract fun monarchDao(): MonarchDao

    companion object {
        @Volatile
        private var INSTANCE: SystemDatabase? = null

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // UserEntity
                database.execSQL("ALTER TABLE user_table ADD COLUMN charisma INTEGER NOT NULL DEFAULT 10")
                database.execSQL("ALTER TABLE user_table ADD COLUMN luck INTEGER NOT NULL DEFAULT 10")

                // QuestEntity
                database.execSQL("ALTER TABLE quest_table ADD COLUMN requirementTarget INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE quest_table ADD COLUMN requirementUnit TEXT NOT NULL DEFAULT 'Count'")
                database.execSQL("ALTER TABLE quest_table ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'E'")
                database.execSQL("ALTER TABLE quest_table ADD COLUMN currentProgress INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE quest_table ADD COLUMN generatedByClass TEXT DEFAULT NULL")

                // GateEntity
                database.execSQL("ALTER TABLE gate_table ADD COLUMN durationMillis INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE gate_table ADD COLUMN failCondition TEXT DEFAULT NULL")

                // BossEntity
                database.execSQL("ALTER TABLE boss_table ADD COLUMN rank TEXT NOT NULL DEFAULT 'E'")
                database.execSQL("ALTER TABLE boss_table ADD COLUMN requiredAwareness INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE boss_table ADD COLUMN requiredCharisma INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE boss_table ADD COLUMN requiredLuck INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE boss_table ADD COLUMN dueDate INTEGER NOT NULL DEFAULT 0")

                // ShadowEntity
                database.execSQL("ALTER TABLE shadow_table ADD COLUMN boostMultiplier REAL NOT NULL DEFAULT 1.0")
                database.execSQL("ALTER TABLE shadow_table ADD COLUMN unlockCondition TEXT NOT NULL DEFAULT ''")

                // MonarchEntity
                database.execSQL("CREATE TABLE IF NOT EXISTS `monarch_table` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `victoryCondition` TEXT NOT NULL, `requiredAggregateStats` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // BossEntity: Add isUnlocked
                database.execSQL("ALTER TABLE boss_table ADD COLUMN isUnlocked INTEGER NOT NULL DEFAULT 0")
                // Unlock E-Rank by default
                database.execSQL("UPDATE boss_table SET isUnlocked = 1 WHERE rank = 'E'")
                
                // UserEntity: Migrate HP to 100 if simplistic (3)
                // Check current maxEndurance first? SQL can handle conditional update
                database.execSQL("UPDATE user_table SET maxEndurance = 100, endurance = 100 WHERE id = 1 AND maxEndurance < 10")
            }
        }
            val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // UserEntity: Add onboardingCompleted
                // Default to 1 (true) for existing users to ensure they don't get stuck in setup
                database.execSQL("ALTER TABLE user_table ADD COLUMN onboardingCompleted INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): SystemDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(SecurityUtils.getDatabasePassphrase(context))
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SystemDatabase::class.java,
                    "system_database"
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
