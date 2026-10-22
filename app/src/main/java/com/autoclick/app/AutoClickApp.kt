package com.autoclick.app

import android.app.Application
import androidx.room.Room
import com.autoclick.app.data.AppDatabase
import com.autoclick.app.data.RunLogRepository
import com.autoclick.app.data.ScriptRepository
import com.autoclick.app.service.AccessibilityGateway
import com.autoclick.app.service.AccessibilityGatewayImpl

class AutoClickApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var scriptRepository: ScriptRepository
        private set

    lateinit var runLogRepository: RunLogRepository
        private set

    val gateway: AccessibilityGateway = AccessibilityGatewayImpl()

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            AppDatabase.NAME
        ).fallbackToDestructiveMigration().build()
        scriptRepository = ScriptRepository(database.scriptDao())
        runLogRepository = RunLogRepository(database.runLogDao(), database.stepLogDao())
    }

    companion object {
        @Volatile
        lateinit var instance: AutoClickApp
            private set
    }
}
