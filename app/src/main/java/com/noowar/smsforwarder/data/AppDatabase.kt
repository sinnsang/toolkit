package com.noowar.smsforwarder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ForwardRule::class, ForwardLog::class, MarqueeItem::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): ForwardRuleDao
    abstract fun forwardLogDao(): ForwardLogDao
    abstract fun marqueeDao(): MarqueeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS forward_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        fromNumber TEXT NOT NULL,
                        toNumber TEXT NOT NULL,
                        body TEXT NOT NULL,
                        ruleId INTEGER NOT NULL,
                        success INTEGER NOT NULL,
                        errorMessage TEXT
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE forward_rules ADD COLUMN channelType TEXT NOT NULL DEFAULT 'SMS'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS marquee_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        text TEXT NOT NULL,
                        fontType TEXT NOT NULL,
                        fontSize INTEGER NOT NULL,
                        direction TEXT NOT NULL,
                        textColorKey TEXT NOT NULL,
                        strokeColorKey TEXT NOT NULL,
                        speed INTEGER NOT NULL,
                        ttsEnabled INTEGER NOT NULL,
                        bgColorKey TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sms_forwarder.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
        }
    }
}
