package com.autoclick.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autoclick.app.data.dao.RunLogDao
import com.autoclick.app.data.dao.ScriptDao
import com.autoclick.app.data.dao.StepLogDao
import com.autoclick.app.data.entity.RunLogEntity
import com.autoclick.app.data.entity.ScriptEntity
import com.autoclick.app.data.entity.StepLogEntity

@Database(
    entities = [ScriptEntity::class, RunLogEntity::class, StepLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
    abstract fun runLogDao(): RunLogDao
    abstract fun stepLogDao(): StepLogDao

    companion object {
        const val NAME = "autoclick.db"
    }
}
